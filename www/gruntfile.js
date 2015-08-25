module.exports = function (grunt) {
    grunt.initConfig({
        // define source files and their destinations
        uglify: {
            bootstrap: {
                files: {
                    '_/js/bootstrap.js': [
                        '_/components/js/bootstrap/affix.js',
                        '_/components/js/bootstrap/alert.js',
                        '_/components/js/bootstrap/button.js',
                        '_/components/js/bootstrap/carousel.js',
                        '_/components/js/bootstrap/collapse.js',
                        '_/components/js/bootstrap/dropdown.js',
                        '_/components/js/bootstrap/modal.js',
                        '_/components/js/bootstrap/tooltip.js',
                        '_/components/js/bootstrap/popover.js',
                        '_/components/js/bootstrap/scrollspy.js',
                        '_/components/js/bootstrap/tab.js',
                        '_/components/js/bootstrap/transition.js'
                    ]
                }
            },
            jquery: { 
                src: '_/components/js/jquery/jquery.js',
                dest: '_/js/jquery.js'
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
            css:  { files: '_/components/less/*.less', tasks: [ 'less' ] }
        }
    });

    // load plugins
    grunt.loadNpmTasks('grunt-contrib-watch');
    grunt.loadNpmTasks('grunt-contrib-uglify');
    grunt.loadNpmTasks('grunt-contrib-less');

    // register at least this one task
    grunt.registerTask('default', [ 'uglify', 'less' ]);
};