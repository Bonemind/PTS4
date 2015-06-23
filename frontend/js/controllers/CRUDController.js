//CRUD controller
PTSAppControllers.controller("CRUDController", ["$scope", "Restangular", "messageCenterService", "close", "model", "meta",
	function($scope, Restangular, messageCenterService, close, model, meta) {
		console.log("asdasdasd");
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
