PTSAppControllers.controller("LoginController", ["$rootScope", "$scope", "$http", "messageCenterService", "$location", "Restangular",
		function($rootScope, $scope, $http, messageCenterService, $location, Restangular) {
			$scope.login = function(user) {
				if (user == undefined || user.email == "" || user.password == "") {
					messageCenterService.add("warning", "Fill in all fields", {timeout: 8000});
					return;
				} 
				Restangular.all("auth").all("login").post({"email": user.email, "password": user.password})
 					.then(function(data) {
					 	$rootScope.token = data.token;
					 	$rootScope.user = data.user;
					 	$location.path("/");
					}, function(err) {
						var status = err.status;
						if (status >= 500 && status < 600) {
							messageCenterService.add("danger", "The server seems to be having trouble, please try again later", {timeout: 8000});
						} else if (status == 401) {
							messageCenterService.add("danger", "Invalid username or password", {timeout: 8000});
						} else {
							messageCenterService.add("danger", "Something went wrong, try again later", {timeout: 8000});
						}
					});
			};
	}
]);
