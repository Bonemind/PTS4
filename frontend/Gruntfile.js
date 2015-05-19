	module.exports = function(grunt) {
	grunt.loadNpmTasks('grunt-serve');
	grunt.loadNpmTasks('grunt-contrib-concat');
	grunt.initConfig({
		serve: {
		    options: {
			port: 9000
			}
		 },
		concat: {
			basic: {
				src: ['js/controllers/*.js'],
				dest: 'js/controllers.js'
			}
		}
	});

}
