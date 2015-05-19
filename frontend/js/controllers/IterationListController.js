PTSAppControllers.controller("IterationListController", ["$rootScope", "$scope", "Restangular", "ModalService", "$routeParams",
		function($rootScope, $scope, Restangular, ModalService, $routeParams) {
			$scope.update = function() {
				Restangular.one("team", $routeParams.id).get().then(function(team) {
					$scope.team = team;
					team.all("iteration").getList().then(function(iterations) {
						console.log(iterations);
						$scope.iterations = iterations;
					});
				});
			}

			$scope.showModal = function(model) {
				if (model === undefined) {
					model = Restangular.restangularizeElement(null, {name: "", description: "", start: new Date(), end: dateAdd(new Date(), "day", 7), team: $scope.team.id }, "iteration");
				} else {
					model = Restangular.copy(model);
					model.parentResource = null;
					model.start = new Date(model.start);
					model.end = new Date(model.end);
				}
				
				var copiedStatus = cleanupStatusList($rootScope.user, $rootScope.StatusList);
				ModalService.showModal({
					templateUrl: "templates/IterationModal.html",
					controller: "CRUDController",
					inputs: {
						model: model,
						meta: {
						}
					}

				}).then(function(modal) {
					modal.element.modal();
					modal.close.then(function(result) {
						$scope.update();
					});
				});
			}
			$scope.update();
		}]);

