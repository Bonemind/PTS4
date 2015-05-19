PTSAppControllers.controller("TaskListController", ["$rootScope", "$scope", "Restangular", "ModalService", "$routeParams",
		function($rootScope, $scope, Restangular, ModalService, $routeParams) {
			$scope.update = function() {
				Restangular.one("story", $routeParams.id).get().then(function (story) {
					$scope.story = story
					$scope.story.getList("task")
					.then(function(tasks) {
						$scope.tasks = tasks;
					});
					$scope.story.getList("test")
						.then(function(tests) {
							$scope.tests = tests;
						});
					Restangular.one("project", $scope.story.project).get()
						.then(function(project) {
							$scope.project = project;
						});
				});
			}

			$scope.showTaskModal = function(model) {
				if (model === undefined) {
					model = Restangular.restangularizeElement(null, {name: "", description: "", status: "DEFINED", story: $scope.story.id }, "task");
				} else {
					model = Restangular.copy(model);
					model.parentResource = null;
				}
				var copiedStatus = angular.copy($rootScope.StatusList);
				copiedStatus = _.filter(copiedStatus, function(status) {
					return status.status !== "ACCEPTED";
				});
				ModalService.showModal({
					templateUrl: "templates/TaskModal.html",
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

			$scope.showTestModal = function(model) {
				if (model === undefined) {
					model = Restangular.restangularizeElement(null, {name: "", description: "", accepted: false, story: $scope.story.id }, "test");
				} else {
					model = Restangular.copy(model);
					model.parentResource = null;
				}
				ModalService.showModal({
					templateUrl: "templates/TestModal.html",
					controller: "CRUDController",
					inputs: {
						model: model,
						meta: {
							project: $scope.project
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

