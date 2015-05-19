
PTSAppControllers.controller("LoginMenuController", ["$rootScope", "$scope", "Restangular",
		function($rootScope, $scope, Restangular) {
			$scope.loggedIn = false;
			$rootScope.$watch("user", function(newval, oldval) {
				$scope.loggedIn = newval !== undefined;
			});
			$scope.logout = function() {
			 	Restangular.all("auth").all("logout").post({})
 					.then(function() {
					     $rootScope.user = undefined;
					     $rootScope.token = undefined;
					     localStorage.removeItem("token");
					     localStorage.removeItem("user");
					});
			}
		}]);
