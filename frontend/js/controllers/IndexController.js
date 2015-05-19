PTSAppControllers.controller("IndexContoller", ["$rootScope", "$scope", 
		function($rootScope, $scope) {
			$scope.isLoggedIn = function() {
				return $rootScope.token !== undefined;
			}
}]);
