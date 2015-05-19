PTSAppControllers.controller("TeamListController", ["$scope", "Restangular", "ModalService",
		function($scope, Restangular, ModalService) {
			$scope.update = function() {
				teamList = Restangular.all("team").getList()
				.then(function(teams) {
					$scope.teams = teams;
				});
			}
			$scope.showModal = function(model) {
				if (model === undefined) {
					model = Restangular.restangularizeElement(null, {name: "" }, "team");
				} else {
					model = Restangular.copy(model);
				}
				ModalService.showModal({
					templateUrl: "templates/TeamModal.html",
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
		}
	]);
