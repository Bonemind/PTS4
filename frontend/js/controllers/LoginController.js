PTSAppControllers.controller("LoginController", ["$rootScope", "$scope", "$http", "messageCenterService", "$location",
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