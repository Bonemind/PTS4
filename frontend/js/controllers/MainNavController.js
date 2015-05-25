PTSAppControllers.controller("MainNavController", ["$rootScope", "$scope", "Restangular", "$location",
	function($rootScope, $scope, Restangular, $location) {
	    	$scope.currentTeam = undefined;
	    	$scope.currentProject = undefined;
	 	$scope.update = function() {
	 	    	if ($location.url().indexOf("register") >= 0) {
			    return;
			}
			Restangular.all("team").getList()
 				.then(function(teams) {
				 	$scope.teams = teams;
				});
			Restangular.all("project").getList()
    				.then(function(projects) {
				 	$scope.projects = projects;
				});
		}
		$scope.teamSelect = function(team) {
		    $scope.currentTeam = team;
		}
		$scope.projectSelect = function(project) {
		    $scope.currentProject = project;
		}
		$rootScope.$on("login", $scope.update);
		$rootScope.$on("project-created", $scope.update);
		$rootScope.$on("team-created", $scope.update);
		$scope.update();
	}
]);
