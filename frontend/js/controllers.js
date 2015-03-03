var PTSAppControllers = angular.module("PTSAppControllers", []);

PTSAppControllers.controller("loginController", ["$rootScope", "$scope", "$http", "messageCenterService", "$location",
		function($rootScope, $scope, $http, messageCenterService, $location) {
			console.log("logincontroller");

			$scope.login = function(user) {
				if (user == undefined || user.email == "" || user.password == "") {
					messageCenterService.add("warning", "Fill in all fields", {timeout: 8000});
					return;
				} 
				$http.post("http://192.168.1.202:8182/auth/login", {"email": user.email, "password": user.password}).
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
