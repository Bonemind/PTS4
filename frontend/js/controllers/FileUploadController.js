//CRUD controller
PTSAppControllers.controller("FileUploadController", ["$rootScope", "$scope", "Restangular", "messageCenterService", "close", "model", "meta",
	function($rootScope, $scope, Restangular, messageCenterService, close, model, meta) {
		$scope.model = model;
		$scope.meta = meta;
		$scope.close = function(result) {
			close(result, 100);
		}
		$scope.save = function(result) {
			var formData = new FormData();
			console.log(result);
			_.each(result, function(val, key) {
			    formData.append(key, val);
			});
			meta.team.one("import/" + meta.importType)
    				.withHttpConfig({transformRequest: angular.identity})
    				.customPOST(formData, '', undefined, {"Content-Type": undefined, "X-Token": $rootScope.token})
    				.then(function() {
				    messageCenterService.add("success", "File Uploaded");
				    $rootScope.$broadcast("project-created");
				    close(result, 100);
				}, function(err) {
					if (err.status == 409) {
					    messageCenterService.add("danger", "That column contains the maximum number of stories", {timeout: 7000});
					close(result, 100);
					} else {
					    messageCenterService.add("danger", "Something went wrong, please try again", {timeout: 7000});
					}
					close(result, 100);
				});
		}
	}]);
