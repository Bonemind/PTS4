PTSAppControllers.controller("DashboardController", ["$rootScope", "$scope", "Restangular", 
	function($rootScope, $scope, Restangular) {
	 	$scope.update = function() {
			Restangular.all("task").getList()
 				.then(function(tasks) {
				 	$scope.tasks = tasks;
				});
			Restangular.all("iteration").getList()
 				.then(function(iterations) {
				 	$scope.iterations = iterations;
				});
		}
		$scope.update();
	}
]);
