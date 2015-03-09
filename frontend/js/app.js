var PTSApp = angular.module("PTSApp", [
	"ngRoute",
	"PTSAppControllers",
	"angular-loading-bar",
	"restangular",
	"angularModalService",
	"MessageCenterModule"
	]).run(["$rootScope", "$injector", function($rootScope, $injector) {
		$injector.get("$http").defaults.transformRequest = function(data, headersGetter) {
			if ($rootScope.token !== undefined) {
				headersGetter()["X-Token"] = $rootScope.token;
				console.log("headerstoken:" + $rootScope.token);
			}
			if (data) {
				return angular.toJson(data);
			}
		}
	}]);

//Configure restangular
PTSApp.config(["RestangularProvider", function(RestangularProvider) {
	RestangularProvider.setBaseUrl("http://localhost:8128/api/");
	RestangularProvider.setRequestSuffix('/');
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
		});
	}]);

