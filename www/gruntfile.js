module.exports = function (grunt) {
    grunt.initConfig({
        // define source files and their destinations
        uglify: {
            files: { 
                src: '_/components/**/*.js',  // source files mask
                dest: '_/js/bootstrap.js',    // destination file
            }
        },
        less: {
          development: {
            options: {
              compress: true,
              yuicompress: true,
              optimization: 2
            },
            files: {
                "_/css/bootstrap.css": "_/components/less/bootstrap.less",
                "_/css/custom.css": "_/components/less/_custom.less"
            }
          }
        },
        watch: {
            js:  { files: '_/components/js/*.js', tasks: [ 'uglify' ] },
            css:  { files: '_/components/less/*.less', tasks: [ 'less' ] },
        }
    });

    // load plugins
    grunt.loadNpmTasks('grunt-contrib-watch');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-contrib-less');

    // register at least this one task
    grunt.registerTask('default', [ 'uglify', 'less' ]);
};