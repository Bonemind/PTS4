var PTSAppControllers = angular.module("PTSAppControllers", []);

PTSAppControllers.controller("loginController", ["$rootScope", "$scope", "$http", "messageCenterService", "$location",
		function($rootScope, $scope, $http, messageCenterService, $location) {
			console.log("logincontroller");

			$scope.login = function(user) {
				if (user == undefined || user.email == "" || user.password == "") {
					messageCenterService.add("warning", "Fill in all fields", {timeout: 8000});
					return;
				} 
				$http.post("http://localhost:8182/auth/login", {"email": user.email, "password": user.password}).
					success(function(data, status, headers, config) {
						messageCenterService.add("success", "Logged in", {timeout: 8000, status: messageCenterService.status.next});
						$rootScope.token = data.token;
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

PTSAppControllers.controller("indexContoller", ["$rootScope", "$scope", 
		function($rootScope, $scope) {
			console.log("Indexcontroller");
			$scope.isLoggedIn = function() {
				return $rootScope.token !== undefined;
			}
			console.log($rootScope.token);
}]);

//CRUD controller
PTSAppControllers.controller("CRUDController", ["$scope", "Restangular", "messageCenterService", "close", "model", "meta",
	function($scope, Restangular, messageCenterService, close, model, meta) {
		$scope.model = model;
		$scope.meta = meta;
		$scope.close = function(result) {
			close(result, 500);
		}
		$scope.save = function(result) {
			result.save().then(function() {
				messageCenterService.add("success", "Changes saved", {timeout: 7000});
			}, function() {
				messageCenterService.add("danger", "Something went wrong, please try again", {timeout: 7000});
			});
			close(result, 500);
		}

		$scope.remove = function(result) {
			result.remove().then(function() {
				messageCenterService.add("success", "Changes saved", {timeout: 7000});
			}, function() {
				messageCenterService.add("danger", "Something went wrong, please try again", {timeout: 7000});
			});
			close(result, 500);
		}

	}
]);


PTSAppControllers.controller("TestController", ["$scope", "ModalService",
		function($scope, ModalService) {
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
	]);
