	module.exports = function(grunt) {
	grunt.loadNpmTasks('grunt-serve');
	grunt.loadNpmTasks('grunt-contrib-concat');
	grunt.loadNpmTasks('grunt-contrib-watch');
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
		},
		watch: {
			html: {
				files: "templates/*",
				options: {
					livereload: true
				}
			},
			scripts: {
				files: "js/controllers/*.js",
				tasks: "concat",
				options: {
					livereload: true
				}
			}
		}
	});

}
