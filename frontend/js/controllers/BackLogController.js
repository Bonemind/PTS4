PTSAppControllers.controller("BacklogController", ["$rootScope", "$scope", "Restangular", "ModalService", "$routeParams", "messageCenterService",
		function($rootScope, $scope, Restangular, ModalService, $routeParams, messageCenterService) {
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

			$scope.trackEffort = function(task) {
				task.parentResource = null;
				var model = Restangular.restangularizeElement(task, {effort: 0}, "effort");
				ModalService.showModal({
					templateUrl: "templates/EffortModal.html",
					controller: "CRUDController",
					inputs: {
						model: model,
						meta: {
						}
					}

				}).then(function(modal) {
					modal.element.modal();
					modal.close.then(function(result) {
						$scope.update();
					});
				});
			}

			$scope.storyDropped = function(e, index, droppedStory, external, type, status) {
				$scope.stories = _.filter($scope.stories, function(story) {
					return droppedStory.id != story.id;
				});
				droppedStory.status = status;
				console.log(droppedStory);
				var restangularized = Restangular.restangularizeElement(null, droppedStory, "story");
				restangularized.put()
					.then(function() {
						messageCenterService.add('success', "Changes saved");
					}, function(err) {
						if (err.status == 409) {
							messageCenterService.add("danger", "You can not add more stories in that column");
						} else if (err.status) {
							messageCenterService.add("danger", "You are not allowed to do that");
						}
						$scope.update();
					});
				return droppedStory;
			}

			$scope.taskDropped = function(e, index, droppedTask, external, type, status, story) {
				console.log(story);
				console.log(droppedTask);
				story.tasks = _.filter(story.tasks, function(task) {
					return droppedTask.id != task.id;
				});
				droppedTask.status = status;
				console.log("aaaaaaaaa");
				var restangularized = Restangular.restangularizeElement(null, droppedTask, "task");
				restangularized.put()
					.then(function() {
						messageCenterService.add('success', "Changes saved");
					}, function(err) {
						if (err.status) {
							messageCenterService.add("danger", "You are not allowed to do that");
						}
						$scope.update();
					});
				console.log("bbbbbbb");
				return droppedTask;
			}
			$scope.labels = ["Jan", "Feb"];
			$scope.series = ["Iteration", "Ideal"];
			$scope.data = [["1", "2"], ["3", "4"]];
			$scope.showBurndown = function() {
				//We haven't selected an iteration, generating a burndown would be none sensible
				if ($scope.selectediteration.id === undefined) {
					return;
				}

				//Determine the start dates, and number of days in the iteration
				var dayLength = 3600 * 1000 * 24; 
				var startDate = new Date($scope.selectediteration.start);
				var totaldays = new Date($scope.selectediteration.end).getTime() - new Date($scope.selectediteration.start).getTime();
				totaldays /= dayLength;
				totaldays = Math.ceil(totaldays);
					
				//Determine the total number of points in the iteration
				//The total number of points in the sprint, including stories added later
				var totalPoints = 0;

				//The points at the start of the sprint
				var sprintStartPoints = 0;
				var datapoints = []
				$scope.stories.forEach(function(story) {
					//If this story was added before, or on the startdate of the iteration, add it's storypoints to the total
					totalPoints += story.points;
					if (new Date(story.iterationSetOn).setHours(0, 0, 0) <= startDate) {
						sprintStartPoints += story.points;
					} else {
						completedOn = new Date(new Date(story.iterationSetOn).toISOString().slice(0,10)).toLocaleDateString();
						//The story was added mid-iteration, for the purposes of the chart, a negative amount of work was done
						if (datapoints[completedOn] == undefined) {
							datapoints[completedOn] = 0;
						}
						datapoints[completedOn] -= story.points;
					}
					if (story.completedOn !== undefined && story.completedOn !== null) {
						completedOn = new Date(new Date(story.completedOn).toISOString().slice(0,10)).toLocaleDateString();
						if (datapoints[completedOn] == undefined) {
							datapoints[completedOn] = 0;
						}
						datapoints[completedOn] += story.points;
					}
				});

				//Determine the ideal burndown graph
				var keys = [];
				var vals = [];
				var ideal = _.range(totalPoints, 0, totalPoints / (1 - totaldays));
				ideal.push(0);

				//The number of points we have sprintPointsLeft in the sprint
				var sprintPointsLeft = sprintStartPoints;

				//Determine the actual burndown graph
				for (var i=0; i < totaldays; i++) {
					var currDate = new Date(startDate.getTime() + i * dayLength);
					var currDateString = currDate.toLocaleDateString();
					//If we aren't looking at dates in the future, remove the number of story points done
					//on this date, from the number of story points left
					if (currDate < new Date()) {
						sprintPointsLeft -= datapoints[currDateString] || 0;
						vals.push(sprintPointsLeft);
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

