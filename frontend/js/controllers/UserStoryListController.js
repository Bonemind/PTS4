PTSAppControllers.controller("UserStoryListController", ["$rootScope", "$routeParams", "$scope", "Restangular", "ModalService",
		function($rootScope, $routeParams, $scope, Restangular, ModalService) {
			$scope.update = function() {
			    	Restangular.one("team", $routeParams.id).get()
    					.then(function(team) {
					    $scope.team = team;
					    team.all("story").getList()
					    .then(function(stories) {
						    $scope.stories = stories;
						    console.log(stories);
					    });
					});
			}

			$scope.storyDropped = function(e, index, item) {
			    var sorted = _.sortBy($scope.stories, "priority");
			    var filtered = _.filter(sorted, function(s) { return s.id != item.id });
			    filtered.splice(index - 1, 0, item);
			    
			    for (var i = 0; i < filtered.length; i++) {
				Restangular.restangularizeElement(null, {id: filtered[i].id, priority: i}, "story").put()
				    .then(function() {
					$scope.update();
				});
			    }
			    return item;
			}

			$scope.update();
		}]);
