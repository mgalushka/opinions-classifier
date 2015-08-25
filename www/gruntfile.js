module.exports = function (grunt) {
    grunt.initConfig({
        // define source files and their destinations
        uglify: {
            bootstrap: { 
                src: '_/components/js/bootstrap/*.js',  // source files mask
                dest: '_/js/bootstrap.js',    // destination file
            },
            jquery: { 
                src: '_/components/js/jquery/*.js',
                dest: '_/js/jquery.js',
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
            js:  { files: '_/components/js/**/*.js', tasks: [ 'uglify' ] },
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