var PTSApp = angular.module("PTSApp", [
	"ngRoute",
	"PTSAppControllers",
	"angular-loading-bar",
	"restangular",
	"angularModalService",
	"MessageCenterModule"
	]).run(["$rootScope", "$injector", function($rootScope, $injector) {

		$rootScope.StatusList = [
			{status: "DEFINED", text : "Defined"},
			{status: "IN_PROGRESS", text: "In Progress"},
			{status: "DONE", text: "Done"}, 
			{status: "ACCEPTED", text: "Accepted"}
		];
		$injector.get("$http").defaults.transformRequest = function(data, headersGetter) {
			console.log($rootScope.token);
			if ($rootScope.token !== undefined) {
				headersGetter()["X-Token"] = $rootScope.token;
				headersGetter()["X-Token"] = "test";
			}
			if (data) {
				return angular.toJson(data);
			}
		}
	}]);

//Configure restangular
PTSApp.config(["RestangularProvider", function(RestangularProvider) {
	RestangularProvider.setBaseUrl("http://localhost:8182/");
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
		}).when("/test", {
			templateUrl: "templates/test.html",
			controller: "TestController"
		}).when("/stories", {
			templateUrl: "templates/UserStoryList.html",
			controller: "UserStoryListController"
		}).when("/story/:id/tasks", {
			templateUrl: "templates/TaskList.html",
			controller: "TaskListController"
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
