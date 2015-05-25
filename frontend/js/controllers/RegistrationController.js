PTSAppControllers.controller("RegistrationController", ["$scope", "Restangular", "messageCenterService", "$location", 
		function($scope, Restangular, messageCenterService, $location) {
			$scope.user = {"email": "", "name": "", "password": "", "passwordconfirm": ""};

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

