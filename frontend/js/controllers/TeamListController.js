PTSAppControllers.controller("TeamListController", ["$rootScope", "$scope", "Restangular", "ModalService",
		function($rootScope, $scope, Restangular, ModalService) {
		    	$scope.allTeamMembers = [];
			$scope.update = function() {
    				$scope.allTeamMembers = [];
				teamList = Restangular.all("team").getList()
				.then(function(teams) {
					$scope.teams = teams;
					$scope.teams.forEach(function(team) {
					    team.all("user").getList()
					    	.then(function(users) {
						    $scope.allTeamMembers = $scope.allTeamMembers.concat(users);
						});
					});
				});
			}
			$scope.showModal = function(model) {
				if (model === undefined) {
					model = Restangular.restangularizeElement(null, {name: "" }, "team");
				} else {
					model = Restangular.copy(model);
				}

				if (model.kanbanRules) {
				    model.enableKanban = true;
				}

				ModalService.showModal({
					templateUrl: "templates/TeamModal.html",
					controller: "TeamCRUDController",
					inputs: {
						model: model,
						meta: {
						}
					}

				}).then(function(modal) {
					modal.element.modal();
					modal.close.then(function(result) {
					    $rootScope.$broadcast("team-created");
					    $scope.update();
					});
				});
			}
			$scope.update();
		}
	]);
