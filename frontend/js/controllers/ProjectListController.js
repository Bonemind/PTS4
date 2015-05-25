PTSAppControllers.controller("ProjectListController", ["$rootScope", "$scope", "Restangular", "ModalService", "$routeParams",
		function($rootScope, $scope, Restangular, ModalService, $routeParams) {
		    	$scope.teammembers = [];
			$scope.update = function() {
				Restangular.one("team", $routeParams.id).get().then(function (team) {
					$scope.team = team
					$scope.team.getList("project")
						.then(function(projects) {
							$scope.projects = projects;
						});
					$scope.team.getList("user")
				    		.then(function(members) {
				    		    	$scope.teammembers = members;
						});
					Restangular.all("user").getList()
					.then(function(users) {
						$scope.allUsers = users;
					});
				});
			}

			$scope.showModal = function(model) {
				if (model === undefined) {
					model = Restangular.restangularizeElement(null, {name: "", description: "", team: $scope.team.id}, "project");
				} else {
					model = Restangular.copy(model);
					model.team = $scope.team.id;
					model.parentResource = null;
					model.productOwner = model.productOwner.email;
				}
				ModalService.showModal({
					templateUrl: "templates/ProjectModal.html",
					controller: "CRUDController",
					inputs: {
						model: model,
						meta: {
						    allUsers: $scope.allUsers
						}
					}

				}).then(function(modal) {
					modal.element.modal();
					modal.close.then(function(result) {
					    	$rootScope.$broadcast("project-created");
						$scope.update();
					});
				});
			}
			$scope.update();
		}]);


