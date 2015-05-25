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


//CRUD controller
PTSAppControllers.controller("CRUDController", ["$scope", "Restangular", "messageCenterService", "close", "model", "meta",
	function($scope, Restangular, messageCenterService, close, model, meta) {
		$scope.model = model;
		$scope.meta = meta;
		$scope.close = function(result) {
			close(result, 100);
		}
		$scope.save = function(result) {
			console.log(result);
			result.save().then(function() {
				messageCenterService.add("success", "Changes saved", {timeout: 7000});
				close(result, 100);
			}, function(err) {
			    	if (err.status == 409) {
				    messageCenterService.add("danger", "That column contains the maximum number of stories", {timeout: 7000});
				} else {
				    messageCenterService.add("danger", "Something went wrong, please try again", {timeout: 7000});
				}
				close(result, 100);
			});
		}

		$scope.remove = function(result) {
			result.remove().then(function() {
				messageCenterService.add("success", "Changes saved", {timeout: 7000});
				close(result, 100);
			}, function() {
				messageCenterService.add("danger", "Something went wrong, please try again", {timeout: 7000});
				close(result, 100);
			});
		}

	}
]);

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

PTSAppControllers.controller("IndexContoller", ["$rootScope", "$scope", 
		function($rootScope, $scope) {
			$scope.isLoggedIn = function() {
				return $rootScope.token !== undefined;
			}
}]);

PTSAppControllers.controller("IterationListController", ["$rootScope", "$scope", "Restangular", "ModalService", "$routeParams",
		function($rootScope, $scope, Restangular, ModalService, $routeParams) {
			$scope.update = function() {
				Restangular.one("team", $routeParams.id).get().then(function(team) {
					$scope.team = team;
					team.all("iteration").getList().then(function(iterations) {
						console.log(iterations);
						$scope.iterations = iterations;
					});
				});
			}

			$scope.showModal = function(model) {
				if (model === undefined) {
					model = Restangular.restangularizeElement(null, {name: "", description: "", start: new Date(), end: dateAdd(new Date(), "day", 7), team: $scope.team.id }, "iteration");
				} else {
					model = Restangular.copy(model);
					model.parentResource = null;
					model.start = new Date(model.start);
					model.end = new Date(model.end);
				}
				
				var copiedStatus = cleanupStatusList($rootScope.user, $rootScope.StatusList);
				ModalService.showModal({
					templateUrl: "templates/IterationModal.html",
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
			$scope.update();
		}]);


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
					 	localStorage.setItem("token", data.token);
					 	localStorage.setItem("user", JSON.stringify(data.user));
					 	$location.path("/dashboard");
					 	$rootScope.$broadcast("login");
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


PTSAppControllers.controller("LoginMenuController", ["$rootScope", "$scope", "Restangular",
		function($rootScope, $scope, Restangular) {
			$scope.loggedIn = false;
			$rootScope.$watch("user", function(newval, oldval) {
				$scope.loggedIn = newval !== undefined;
			});
			$scope.logout = function() {
			 	Restangular.all("auth").all("logout").post({});
				$rootScope.user = undefined;
				$rootScope.token = undefined;
				localStorage.removeItem("token");
				localStorage.removeItem("user");

			}
		}]);

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

PTSAppControllers.controller("ProjectListController", ["$rootScope", "$scope", "Restangular", "ModalService", "$routeParams",
		function($rootScope, $scope, Restangular, ModalService, $routeParams) {
		    	$scope.teammembers = [];
			$scope.update = function() {
				Restangular.one("team", $routeParams.id).get().then(function (team) {
					$scope.team = team
					$scope.team.getList("project")
						.then(function(projects) {
							$scope.projects = projects;
						});
					$scope.team.getList("user")
				    		.then(function(members) {
				    		    	$scope.teammembers = members;
						});
					Restangular.all("user").getList()
					.then(function(users) {
						$scope.allUsers = users;
					});
				});
			}

			$scope.showModal = function(model) {
				if (model === undefined) {
					model = Restangular.restangularizeElement(null, {name: "", description: "", team: $scope.team.id}, "project");
				} else {
					model = Restangular.copy(model);
					model.team = $scope.team.id;
					model.parentResource = null;
					model.productOwner = model.productOwner.email;
				}
				ModalService.showModal({
					templateUrl: "templates/ProjectModal.html",
					controller: "CRUDController",
					inputs: {
						model: model,
						meta: {
						    allUsers: $scope.allUsers
						}
					}

				}).then(function(modal) {
					modal.element.modal();
					modal.close.then(function(result) {
					    	$rootScope.$broadcast("project-created");
						$scope.update();
					});
				});
			}
			$scope.update();
		}]);



PTSAppControllers.controller("RegistrationController", ["$scope", "$routeParams", "Restangular", "messageCenterService", "$location",
		function($scope, $routeParams, Restangular, messageCenterService, $location) {
			$scope.user = {"email": "", "name": "", "password": "", "passwordconfirm": ""};
			if ($routeParams.email) {
			}
			$scope.user.email = $routeParams.email;

			$scope.register = function(user) {
				if (user.email.trim() == "" || user.password == "" || user.name == "") {
					messageCenterService.add("danger", "Email, username or password cannot be empty", { timeout: 8000 });
					return;
				}
				if (user.password != user.passwordconfirm) {
					messageCenterService.add("danger", "Passwords don't match", { timeout: 8000 });
					return;
				}
				model = Restangular.restangularizeElement(null, {email: user.email, name: user.name, password: user.password }, "user");
				model.save().then(function() {
					messageCenterService.add("success", "Your account has been created, feel free to login", { timeout: 8000, status: messageCenterService.status.next});
					$location.path("/login");
				}, function(error) {
					if (error.status == 409) {
						messageCenterService.add("danger", "Email already in use", { timeout: 8000 });
					}
				});
			}
		}
	]);


PTSAppControllers.controller("TaskListController", ["$rootScope", "$scope", "Restangular", "ModalService", "$routeParams",
		function($rootScope, $scope, Restangular, ModalService, $routeParams) {
			$scope.update = function() {
				Restangular.one("story", $routeParams.id).get().then(function (story) {
					$scope.story = story
					$scope.story.getList("task")
					.then(function(tasks) {
						$scope.tasks = tasks;
					});
					$scope.story.getList("test")
						.then(function(tests) {
							$scope.tests = tests;
						});
					Restangular.one("project", $scope.story.project).get()
						.then(function(project) {
							$scope.project = project;
						});
				});
			}

			$scope.showTaskModal = function(model) {
				if (model === undefined) {
					model = Restangular.restangularizeElement(null, {name: "", description: "", status: "DEFINED", story: $scope.story.id }, "task");
				} else {
					model = Restangular.copy(model);
					model.parentResource = null;
				}
				var copiedStatus = angular.copy($rootScope.StatusList);
				copiedStatus = _.filter(copiedStatus, function(status) {
					return status.status !== "ACCEPTED";
				});
				ModalService.showModal({
					templateUrl: "templates/TaskModal.html",
					controller: "CRUDController",
					inputs: {
						model: model,
						meta: {
							StatusList: copiedStatus,
						}
					}

				}).then(function(modal) {
					modal.element.modal();
					modal.close.then(function(result) {
						$scope.update();
					});
				});
			}

			$scope.showTestModal = function(model) {
				if (model === undefined) {
					model = Restangular.restangularizeElement(null, {name: "", description: "", accepted: false, story: $scope.story.id }, "test");
				} else {
					model = Restangular.copy(model);
					model.parentResource = null;
				}
				ModalService.showModal({
					templateUrl: "templates/TestModal.html",
					controller: "CRUDController",
					inputs: {
						model: model,
						meta: {
							project: $scope.project
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


//CRUD controller
PTSAppControllers.controller("TeamCRUDController", ["$scope", "Restangular", "messageCenterService", "close", "model", "meta",
	function($scope, Restangular, messageCenterService, close, model, meta) {
		$scope.model = model;
		$scope.meta = meta;
		$scope.close = function(result) {
			close(result, 100);
		}
		$scope.save = function(result) {
		    	if (!result.enableKanban) {
			    result.kanbanRules = null;
			}
			delete result.enableKanban;
			result.save().then(function() {
				messageCenterService.add("success", "Changes saved", {timeout: 7000});
			    close(result, 100);
			}, function() {
				messageCenterService.add("danger", "Something went wrong, please try again", {timeout: 7000});
			    close(result, 100);
			});
		}

		$scope.remove = function(result) {
			result.remove().then(function() {
				messageCenterService.add("success", "Changes saved", {timeout: 7000});
				close(result, 100);
			}, function() {
				messageCenterService.add("danger", "Something went wrong, please try again", {timeout: 7000});
				close(result, 100);
			});
		}

	}
]);

PTSAppControllers.controller("TeamListController", ["$rootScope", "$scope", "Restangular", "ModalService",
		function($rootScope, $scope, Restangular, ModalService) {
		    	$scope.allTeamMembers = [];
			$scope.update = function() {
    				$scope.allTeamMembers = [];
				teamList = Restangular.all("team").getList()
				.then(function(teams) {
					$scope.teams = teams;
					$scope.teams.forEach(function(team) {
					    team.all("user").getList()
					    	.then(function(users) {
						    $scope.allTeamMembers = $scope.allTeamMembers.concat(users);
						});
					});
				});
			}
			$scope.showModal = function(model) {
				if (model === undefined) {
					model = Restangular.restangularizeElement(null, {name: "" }, "team");
				} else {
					model = Restangular.copy(model);
				}

				if (model.kanbanRules) {
				    model.enableKanban = true;
				}

				ModalService.showModal({
					templateUrl: "templates/TeamModal.html",
					controller: "TeamCRUDController",
					inputs: {
						model: model,
						meta: {
						}
					}

				}).then(function(modal) {
					modal.element.modal();
					modal.close.then(function(result) {
					    $rootScope.$broadcast("team-created");
					    $scope.update();
					});
				});
			}
			$scope.update();
		}
	]);

PTSAppControllers.controller("TeamViewController", ["$rootScope", "$scope", "Restangular", "ModalService", "$routeParams",
		function($rootScope, $scope, Restangular, ModalService, $routeParams) {
		    	$scope.users = [];
			$scope.update = function() {
				Restangular.one("team", $routeParams.id).get().then(function (team) {
					$scope.team = team;
					$scope.team.getList("user")
					.then(function(users) {
						$scope.users = users;
					});
				});
				Restangular.all("user").getList()
   					.then(function(users) {
					    $scope.allUsers = users;
					});
			}

			$scope.deleteUser = function(user) {
				user.remove().then(function() {
					console.log("removed");
				});
				$scope.update();
			}

			$scope.showModal = function(model) {
				if (model === undefined) {
					model = Restangular.restangularizeElement($scope.team, {name: ""}, "user");
				}
				ModalService.showModal({
					templateUrl: "templates/UserAddModal.html",
					controller: "CRUDController",
					inputs: {
						model: model,
						meta: {
						    allUsers: $scope.allUsers
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


PTSAppControllers.controller("UserStoryListController", ["$rootScope", "$scope", "Restangular", "ModalService",
		function($rootScope, $scope, Restangular, ModalService) {
			$scope.update = function() {
				Restangular.all("story").getList()
				.then(function(stories) {
					$scope.stories = stories;
					console.log(stories);
				});
			}

			$scope.showModal = function(model) {
				if (model === undefined) {
					model = Restangular.restangularizeElement(null, {name: "", description: "", status: "DEFINED" }, "story");
				} else {
					model = Restangular.copy(model);
				}
				
				var copiedStatus = cleanupStatusList($rootScope.user, $rootScope.StatusList);
				ModalService.showModal({
					templateUrl: "templates/UserStoryModal.html",
					controller: "CRUDController",
					inputs: {
						model: model,
						meta: {
							StatusList: copiedStatus
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
