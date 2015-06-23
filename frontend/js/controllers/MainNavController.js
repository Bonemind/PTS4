PTSAppControllers.controller("MainNavController", ["$rootScope", "$scope", "Restangular", "$location", "$http", "ModalService", "messageCenterService",
	function($rootScope, $scope, Restangular, $location, $http, ModalService, messageCenterService) {
	    	$scope.currentTeam = undefined;
	    	$scope.currentProject = undefined;
	 	$scope.update = function() {
	 	    console.log($rootScope.user);
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
	 	    console.log($scope.currentTeam.scrumMaster);
		}
		$scope.projectSelect = function(project) {
		    $scope.currentProject = project;
		}


		$scope.downloadExport = function() {
		    //Retangular doesn't support file downloads, so we do it natively with $http
		    //We need a blob, why? it's a surprise :D
		    $http({method: 'GET', url: $scope.currentTeam.one('export').getRestangularUrl(),
			    headers: {'X-Token': $rootScope.token}, responseType: 'application/xml'}).then(function(response) {

			    //We need to base64 encode the file contents to be able to build a blob
			    var b64encoded = btoa(response.data);

			    //Build the blob and create an object url for it
			    var url = (window.URL || window.webkitURL).createObjectURL(b64toBlob(b64encoded));

			    //Because there is no way to set the file name of a blob, we create an a, append it to the body and set the name there
			    var a = document.createElement("a");
			    document.body.appendChild(a);
			    a.href = url;
			    a.download = "export.xml";

			    //The a was created, now trigger a click, then revoke the objectUrl we created
			    a.click();
			    window.URL.revokeObjectURL(url);
			    a.parentNode.removeChild(a);

	 	    });
		}

		$scope.uploadModal = function(importType) {
		    var importModal = "";
		    var controller = "";
		    var model = {};
		    if (importType == "rally") {
		    	importModal = "RallyUpload.html";
		    	controller = "FileUploadController";
		    } else if (importType == "vone") {
		    	importModal = "VersionOneImport.html";
		    	controller = "CRUDController";
			model = Restangular.restangularizeElement($scope.currentTeam, {url: "", project: "", token: ""}, "import/versionone");
		    } else {
		    	messageCenterService.add("danger", "Import type not found");
		    	return;
		    }
		    ModalService.showModal({
			templateUrl: "templates/" + importModal,
		    	controller: controller,
		    	inputs: {
			    model: model,
		    	    meta: {
				importType: importType,
		    		team: $scope.currentTeam
			    }
			}
		    }).then(function(modal) {
		    	modal.element.modal();
		    	modal.close.then( function(result) { 
		    	} );
		    });
		}

		$rootScope.$on("login", $scope.update);
		$rootScope.$on("project-created", $scope.update);
		$rootScope.$on("team-created", $scope.update);
		$scope.update();
	}
]);
