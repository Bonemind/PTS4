//CRUD controller
PTSAppControllers.controller("FileUploadController", ["$scope", "Restangular", "messageCenterService", "close", "model", "meta",
	function($scope, Restangular, messageCenterService, close, model, meta) {
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
			Restangular.oneUrl("x", "https://cors-anywhere.herokuapp.com/http://requestb.in/1atu8s41")
    				.withHttpConfig({transformRequest: angular.identity})
    				.customPOST(formData, '', undefined, {"Content-Type": undefined})
    				.then(function() {
				    messageCenterService.add("success", "File Uploaded");
				}, function(err) {
					if (err.status == 409) {
					    messageCenterService.add("danger", "That column contains the maximum number of stories", {timeout: 7000});
					} else {
					    messageCenterService.add("danger", "Something went wrong, please try again", {timeout: 7000});
					}
					close(result, 100);
				});
		}
	}]);
