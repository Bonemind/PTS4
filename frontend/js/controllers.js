var PTSAppControllers = angular.module("PTSAppControllers", []);

PTSAppControllers.controller("loginController", ["$rootScope", "$scope", "$http", "messageCenterService", "$location",
		function($rootScope, $scope, $http, messageCenterService, $location) {
			$scope.login = function(user) {
				if (user == undefined || user.email == "" || user.password == "") {
					messageCenterService.add("warning", "Fill in all fields", {timeout: 8000});
					return;
				} 
				$http.post("http://localhost:8182/auth/login", {"email": user.email, "password": user.password}).
					success(function(data, status, headers, config) {
						messageCenterService.add("success", "Logged in", {timeout: 8000, status: messageCenterService.status.next});
						$rootScope.token = data.token;
						$rootScope.user = data.user;
						$location.path("/");
					}).
					error(function(data, status, headers, config) {
						if (status >= 500 && status < 600) {
							messageCenterService.add("danger", "The server seems to be having trouble, please try again later", {timeout: 8000});
						} else if (status == 401) {
							messageCenterService.add("danger", "Invalid username or password", {timeout: 8000});
						} else {
							messageCenterService.add("danger", "Something went wrong, try again later", {timeout: 8000});
						}
						console.log(status);
					});
			};
	}
]);

PTSAppControllers.controller("RegistrationController", ["$scope", "Restangular", "messageCenterService", "$location", 
		function($scope, Restangular, messageCenterService, $location) {
			$scope.user = {"email": "", "password": "", "passwordconfirm": ""};

			$scope.register = function(user) {
				if (user.email.trim() == "" || user.password == "") {
					messageCenterService.add("danger", "Email or password cannot be empty", { timeout: 8000 });
					return;
				}
				if (user.password != user.passwordconfirm) {
					messageCenterService.add("danger", "Passwords don't match", { timeout: 8000 });
					return;
				}
				model = Restangular.restangularizeElement(null, {email: user.email, password: user.password }, "user");
				model.save().then(function() {
					messageCenterService.add("success", "Your account has been created, feel free to login", { timeout: 8000, status: messageCenterService.status.next});
					$location.path("/login");
				}, function(error) {
					if (error.status == 409) {
						messageCenterService.add("danger", "Email already in use", { timeout: 8000 });
					}
				});
			}
		}
	]);

PTSAppControllers.controller("indexContoller", ["$rootScope", "$scope", 
		function($rootScope, $scope) {
			$scope.isLoggedIn = function() {
				return $rootScope.token !== undefined;
			}
}]);

//CRUD controller
PTSAppControllers.controller("CRUDController", ["$scope", "Restangular", "messageCenterService", "close", "model", "meta",
	function($scope, Restangular, messageCenterService, close, model, meta) {
		$scope.model = model;
		$scope.meta = meta;
		$scope.close = function(result) {
			close(result, 100);
		}
		$scope.save = function(result) {
			result.save().then(function() {
				messageCenterService.add("success", "Changes saved", {timeout: 7000});
			}, function() {
				messageCenterService.add("danger", "Something went wrong, please try again", {timeout: 7000});
			});
			close(result, 100);
		}

		$scope.remove = function(result) {
			result.remove().then(function() {
				messageCenterService.add("success", "Changes saved", {timeout: 7000});
			}, function() {
				messageCenterService.add("danger", "Something went wrong, please try again", {timeout: 7000});
			});
			close(result, 100);
		}

	}
]);


PTSAppControllers.controller("TestController", ["$scope", "ModalService",
		function($scope, ModalService) {
			$scope.showModal = function() {
				ModalService.showModal({
					templateUrl: "templates/testModal.html",
					controller: "CRUDController",
					inputs: {
						model: {},
						meta: {}
					}

				}).then(function(modal) {
					modal.element.modal();
					//modal.close();
				});
			}
		}
	]);


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
					templateUrl: "templates/userStoryModal.html",
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


PTSAppControllers.controller("TaskListController", ["$rootScope", "$scope", "Restangular", "ModalService", "$routeParams",
		function($rootScope, $scope, Restangular, ModalService, $routeParams) {
			$scope.update = function() {
				Restangular.one("story", $routeParams.id).get().then(function (story) {
					$scope.story = story
					$scope.story.getList("task")
					.then(function(tasks) {
						$scope.tasks = tasks;
					});
				});
			}

			$scope.showModal = function(model) {
				if (model === undefined) {
					model = Restangular.restangularizeElement($scope.story, {name: "", description: "", status: "DEFINED" }, "task");
				} else {
					model = Restangular.copy(model);
				}
				var copiedStatus = angular.copy($rootScope.StatusList);
				copiedStatus = _.filter(copiedStatus, function(status) {
					return status.status !== "ACCEPTED";
				});
				ModalService.showModal({
					templateUrl: "templates/userStoryModal.html",
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

PTSAppControllers.controller("LoginMenuController", ["$rootScope", "$scope",
		function($rootScope, $scope) {
			$scope.loggedIn = false;
			$rootScope.$watch("user", function(newval, oldval) {
				$scope.loggedIn = newval !== undefined;
			});
			$scope.logout = function() {
				$rootScope.user = undefined;
				$rootScope.token = undefined;
			}
		}]);
