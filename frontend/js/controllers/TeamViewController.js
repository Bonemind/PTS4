PTSAppControllers.controller("TeamViewController", ["$rootScope", "$scope", "Restangular", "ModalService", "$routeParams",
		function($rootScope, $scope, Restangular, ModalService, $routeParams) {
			$scope.update = function() {
				Restangular.one("team", $routeParams.id).get().then(function (team) {
					$scope.team = team;
					$scope.team.getList("user")
					.then(function(users) {
						$scope.users = users;
					});
				});
			}

			$scope.deleteUser = function(user) {
				user.remove().then(function() {
					console.log("removed");
				});
				$scope.update();
			}

			$scope.showModal = function(model) {
				if (model === undefined) {
					model = Restangular.restangularizeElement($scope.team, {email: ""}, "user");
				}
				ModalService.showModal({
					templateUrl: "templates/UserAddModal.html",
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

