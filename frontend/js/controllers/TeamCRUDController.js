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
