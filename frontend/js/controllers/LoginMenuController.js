
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
