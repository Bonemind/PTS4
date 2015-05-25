PTSAppControllers.controller("BacklogController", ["$rootScope", "$scope", "Restangular", "ModalService", "$routeParams",
		function($rootScope, $scope, Restangular, ModalService, $routeParams) {
			$scope.selectediteration = {id: undefined, name: "None"};
			$scope.selectedproject = {id: undefined, name: "None"};
			$scope.update = function() {
				Restangular.one("team", $routeParams.id).get().then(function (team) {
					team.all("iteration").getList()
						.then(function(iterations) {
							$scope.iterations = iterations;
							$scope.iterations.splice(0, 0, {"id": undefined, "name": "None"});
						});

					$scope.team = team;
					team.all("user").getList()
						.then(function(users) {
							$scope.teammembers = users;
						});
					$scope.getFullBacklog();
				});
			}

			$scope.getFullBacklog = function() {
					$scope.stories = [];
					$scope.team.all("project").getList()
						.then(function(projects) {
							$scope.projects = projects;

							if ($routeParams.projectId) {
								$scope.selectedproject = _.find(projects, function(project) {
									return project.id == $routeParams.projectId;
								});
								$scope.getStoriesForProjects([ $scope.selectedproject ]);
							} else {
								$scope.getStoriesForProjects(projects);
							}
					});
					
			}

			$scope.labels = ["Jan", "Feb"];
			$scope.series = ["Iteration", "Ideal"];
			$scope.data = [["1", "2"], ["3", "4"]];
			$scope.showBurndown = function() {
				if ($scope.selectediteration.id === undefined) {
					return;
				}
				var dayLength = 3600 * 1000 * 24; 
				var startDate = new Date($scope.selectediteration.start);
				var totaldays = new Date($scope.selectediteration.end).getTime() - new Date($scope.selectediteration.start).getTime();
				totaldays /= dayLength;
				totaldays = Math.ceil(totaldays);
					
				var totalPoints = 0;
				var datapoints = []
				$scope.stories.forEach(function(story) {
					totalPoints += story.points;
					if (story.completedOn !== undefined && story.completedOn !== null) {
						completedOn = new Date(new Date(story.completedOn).toISOString().slice(0,10)).toLocaleDateString();
						if (datapoints[completedOn] == undefined) {
							datapoints[completedOn] = 0;
						}
						datapoints[completedOn] += story.points;
					}
				});
				var keys = [];
				var vals = [];
				var ideal = _.range(totalPoints, 0, totalPoints / (1 - totaldays));
				ideal.push(0);
				var totalPointsCopy = totalPoints;

				for (var i=0; i < totaldays; i++) {
					var currDate = new Date(startDate.getTime() + i * dayLength);
					var currDateString = currDate.toLocaleDateString();
					if (currDate < new Date()) {
						totalPointsCopy -= datapoints[currDateString] || 0;
						vals.push(totalPointsCopy);
					}
					keys.push(currDateString);
				}
				$scope.data = [vals, ideal];
				$scope.labels = keys;
			}

			$scope.getStoriesForProjects = function(projects) {
				$scope.stories = [];
				projects.forEach(function(project) {
					project.parentResource = null;
					project.all("story").getList()
						.then(function(stories) {
							stories.forEach(function(x) {
								$scope.stories.push(x);
							});
							$scope.injectTasks();
						});
				});
			}

			$scope.updateIteration = function(iteration) {
				if (iteration.id === undefined) {
					$scope.getFullBacklog();
				} else {
					iteration.parentResource = null;
					iteration.all("story").getList()
						.then(function(stories) {
							$scope.stories = stories;
							$scope.injectTasks();
							$scope.showBurndown();
						});
				}
			}

			$scope.updateProject = function(project) {
				var projectArray = [ project ];
				$scope.getStoriesForProjects(projectArray);
			}

			$scope.injectTasks = function() {
				$scope.stories.forEach(function(story) {
					story.parentResource = null;
					story.all("task").getList()
						.then(function(tasks) {
							story.tasks = tasks;
						});
				});
			}

			$scope.claimTask = function(task) {
				task.owner = $rootScope.user.id;
				task.parentResource = null;
				task.save();
			}

			$scope.showModal = function(model) {
				if (model === undefined) {
					model = Restangular.restangularizeElement(null, {name: "", description: "", points: 0, status: "DEFINED", team: $scope.team.id, type: "USER_STORY" }, "story");
				} else {
					model = Restangular.copy(model);
					model.parentResource = null;
				}
				
				var copiedStatus = cleanupStatusList($rootScope.user, $rootScope.StatusList);
				var parentProject = undefined;
				if (model.project != undefined) {
					$scope.projects.forEach(function(project) {
						if (project.id == model.project) {
							parentProject = project;
						}
					});
					
				}
				ModalService.showModal({
					templateUrl: "templates/UserStoryModal.html",
					controller: "CRUDController",
					inputs: {
						model: model,
						meta: {
							StatusList: copiedStatus,
							projectList: $scope.projects,
							iterationList: $scope.iterations,
							parentProject: parentProject,
							storyTypes: $rootScope.StoryTypes
						}
					}

				}).then(function(modal) {
					modal.element.modal();
					modal.close.then(function(result) {
						$scope.update();
					});
				});
			}
			$scope.update();
		}]);

