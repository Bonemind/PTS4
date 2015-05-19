PTSAppControllers.controller("UserStoryListController", ["$rootScope", "$scope", "Restangular", "ModalService",
		function($rootScope, $scope, Restangular, ModalService) {
			$scope.update = function() {
				Restangular.all("story").getList()
				.then(function(stories) {
					$scope.stories = stories;
					console.log(stories);
				});
			}

			$scope.showModal = function(model) {
				if (model === undefined) {
					model = Restangular.restangularizeElement(null, {name: "", description: "", status: "DEFINED" }, "story");
				} else {
					model = Restangular.copy(model);
				}
				
				var copiedStatus = cleanupStatusList($rootScope.user, $rootScope.StatusList);
				ModalService.showModal({
					templateUrl: "templates/UserStoryModal.html",
					controller: "CRUDController",
					inputs: {
						model: model,
						meta: {
							StatusList: copiedStatus
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
