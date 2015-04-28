//Main angular module
var PTSApp = angular.module("PTSApp", [
	"ngRoute",
	"PTSAppControllers",
	"angular-loading-bar",
	"restangular",
	"angularModalService",
	"chart.js",
	"ui.bootstrap.showErrors",
	"MessageCenterModule"
	]).run(["$rootScope", "$injector", function($rootScope, $injector) {

		$rootScope.StatusList = [
			{status: "DEFINED", text : "Defined"},
			{status: "IN_PROGRESS", text: "In Progress"},
			{status: "DONE", text: "Done"}, 
			{status: "ACCEPTED", text: "Accepted"}
		];
		$rootScope.StoryTypes = [
	 		{type: "USER_STORY", text: "Story"},
	 		{type: "DEFECT", text: "Defect"},
		]
		$injector.get("$http").defaults.transformRequest = function(data, headersGetter) {
			if ($rootScope.token !== undefined) {
				headersGetter()["X-Token"] = $rootScope.token;
			}
			if (data) {
				return angular.toJson(data);
			}
		}
	}]);

//Configure restangular
PTSApp.config(["RestangularProvider", function(RestangularProvider) {
	RestangularProvider.setBaseUrl("http://localhost:8182/");
	RestangularProvider.setParentless(false);
	//Make sure a delete request has an empty body
	RestangularProvider.setRequestInterceptor(function(elem, operation) {
	   if (operation === "remove") {
	         return undefined;
	   } 

	   if (operation === "post" || operation === "put") {
		 for (var prop in elem) {
			 if (elem.hasOwnProperty(prop) && _.isDate(elem[prop])) {
				 elem[prop] = elem[prop].toISOString().slice(0,10);
			 }
		 }
	   }
	   return elem;
	});
}]);

//Define routes
PTSApp.config(["$routeProvider",
	function($routeProvider) {
		$routeProvider.when("/login", {
			templateUrl: "templates/login.html",
			controller: "loginController"
		}).when("/", {
			templateUrl: "templates/index.html",
			controller: "indexContoller"
		}).when("/register", {
			templateUrl: "templates/register.html",
			controller: "RegistrationController"
		}).when("/teams", {
			templateUrl: "templates/TeamList.html",
			controller: "TeamListController"
		}).when("/teams/:id/projects", {
		 	templateUrl: "templates/ProjectList.html",
			controller: "ProjectListController"
		}).when("/teams/:id/users", {
		 	templateUrl: "templates/TeamView.html",
			controller: "TeamViewController"
		}).when("/stories", {
			templateUrl: "templates/UserStoryList.html",
			controller: "UserStoryListController"
		}).when("/teams/:id/backlog", {
		 	templateUrl: "templates/Backlog.html",
		 	controller: "BacklogController"
		}).when("/teams/:id/iterations", {
		 	templateUrl: "templates/IterationList.html",
			controller: "IterationListController"
		}).when("/story/:id/tasks", {
			templateUrl: "templates/TaskList.html",
			controller: "TaskListController"
		}).when("/teams/:id/taskboard", {
		 	templateUrl: "templates/TaskBoard.html",
			controller: "BacklogController"
		});
	}]);

/**
 *  Source: https://gist.github.com/asafge/7430497
 *  A generic confirmation for risky actions.
 *  Usage: Add attributes: ng-really-message="Are you sure"? ng-really-click="takeAction()" function
 */
angular.module('PTSApp').directive('ngReallyClick', [function() {
    return {
	restrict: 'A',
	link: function(scope, element, attrs) {
	    element.bind('click', function() {
		var message = attrs.ngReallyMessage;
		if (message && confirm(message)) {
		    scope.$apply(attrs.ngReallyClick);
		}
	    });
	}
    }
}]);

//Setup a basic auth interceptor
//If we're unauthenticated a login dialog is shown
PTSApp.factory("authInterceptor", ["$rootScope", "$q", "$location", "messageCenterService",
	function($rootScope, $q, $location, messageCenterService) {
		return {
			"responseError": function(response) {
				if (response && response.status === 401) {
					var deferred = $q.defer();
					if ($location.url().indexOf("login") <= -1) {
						messageCenterService.add("danger", "Please login first", {timeout: 7000, status: messageCenterService.status.next});
					}
					$location.path("/login");
					return $q.reject(response);
				}
				return $q.reject(response);
			}
		}
	}
]);

//Inject the auth provider
PTSApp.config(["$httpProvider", function ($httpProvider) {
		$httpProvider.interceptors.push("authInterceptor");
}]);

/**
 * Resolves a system status to the human readable variant
 * usage: <pts-status status="somestatus"></pts-status>
 */
angular.module("PTSApp").directive('ptsStatus', ["$rootScope", 
		function($rootScope) {

			return  {
				restrict: 'AEC',
				scope: {
					status: "=status"
				},
				template: "{{ readable }}",
				controller: function($scope, $element, $attrs, $location) {
					var StatusList = $rootScope.StatusList;
					var readable = "None";
					StatusList.forEach(function(status) {
						if (status.status == $scope.status) {
							readable = status.text;
						}
					});
					$scope.readable = readable;
				}
			}
		}]);

angular.module("PTSApp").directive('teamMember', ["$rootScope", 
		function($rootScope) {

			return  {
				restrict: 'AEC',
				scope: {
					userid: "=userid",
					teammembers: "=teammembers"
				},
				template: "{{ readable }}",
				controller: function($scope, $element, $attrs, $location) {
					var readable = "None";
					$scope.teammembers.forEach(function(member) {
						if (member.id == $scope.userid) {
							readable = member.email;
						}
					});
					$scope.readable = readable;
				}
			}
		}]);
/**
 * Resolves a story type to the human readable variant
 * usage: <pts-type type="sometype"></pts-type>
 */
angular.module("PTSApp").directive('ptsType', ["$rootScope", 
		function($rootScope) {

			return  {
				restrict: 'AEC',
				scope: {
					type: "=type"
				},
				template: "{{ readable }}",
				controller: function($scope, $element, $attrs, $location) {
				 	console.log($scope.type);
					var StoryTypes = $rootScope.StoryTypes;
				 	console.log(StoryTypes);
					var readable = "None";
					StoryTypes.forEach(function(type) {
						if (type.type == $scope.type) {
							readable = type.text;
						}
					});
					$scope.readable = readable;
				}
			}
		}]);
/**
 * Resolves a story type to the human readable variant
 * usage: <pts-type type="sometype"></pts-type>
 */
angular.module("PTSApp").directive('backlogStory', ["$rootScope", 
		function($rootScope) {

			return  {
				restrict: 'AECM',
				scope: {
					story: "=story"
				},
				templateUrl: "templates/StoryDirectiveTemplate.html",
				controller: function($scope, $element, $attrs, $location) {
				}
			}
		}]);

/**
 * Used to hide statusses depending on the role of a user
 * user is the current logged in user
 * statusList is the list of statusses
 */
function cleanupStatusList(user, statusList) {
	if (user.role === "PRODUCT_OWNER") {
		return statusList;
	}
	var acceptedIndex = -1;

	var copiedStatus = angular.copy(statusList);
	copiedStatus = _.filter(copiedStatus, function(status) {
	 return status.status !== "ACCEPTED";
	});
	return copiedStatus;
}

function dateAdd(date, interval, units) {
  var ret = new Date(date); //don't change original date
  switch(interval.toLowerCase()) {
    case 'year'   :  ret.setFullYear(ret.getFullYear() + units);  break;
    case 'quarter':  ret.setMonth(ret.getMonth() + 3*units);  break;
    case 'month'  :  ret.setMonth(ret.getMonth() + units);  break;
    case 'week'   :  ret.setDate(ret.getDate() + 7*units);  break;
    case 'day'    :  ret.setDate(ret.getDate() + units);  break;
    case 'hour'   :  ret.setTime(ret.getTime() + units*3600000);  break;
    case 'minute' :  ret.setTime(ret.getTime() + units*60000);  break;
    case 'second' :  ret.setTime(ret.getTime() + units*1000);  break;
    default       :  ret = undefined;  break;
  }
  return ret;
}
