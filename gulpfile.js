var gulp = require('gulp');
var browserify = require('browserify');
var babelify = require('babelify');
var util = require('gulp-util');
var buffer = require('vinyl-buffer');
var source = require('vinyl-source-stream');
// var uglify = require('gulp-uglify');
var sourcemaps = require('gulp-sourcemaps');
var watchify = require("watchify");

gulp.task('dev', function() {
    var b = getBrowserify();

    b = watchify(b);
    b.on("update", function () {
        bundle(b);
    });
    b.on('log', util.log.bind(util));
    b.add("./src/main/es6/app.js");

    bundle(b);
});

gulp.task('build', function() {
    var b = getBrowserify();
    bundle(b);
});

function getBrowserify() {
    return browserify('./src/main/es6/app.js', {
        debug: true
    })
        // .add(require.resolve('babel/polyfill'))
        .transform(babelify);
}

function bundle(b) {
    b.bundle()
        .on('error', util.log.bind(util, 'Browserify Error'))
        .pipe(source('app.js'))

        .pipe(buffer())
        .pipe(sourcemaps.init({
            loadMaps: true
        }))
/*
        .pipe(uglify({
            mangle: false
        }))
*/
        .pipe(sourcemaps.write('./'))
        .pipe(gulp.dest('./src/main/webapp/public/dist/js'));
}

gulp.task('default', ['dev']);
