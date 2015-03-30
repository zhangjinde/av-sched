var babelify = require('babelify');
var browserify = require('browserify');
var buffer = require('vinyl-buffer');
var del = require("del");
var gulp = require('gulp');
var mochify = require("mochify");
var source = require('vinyl-source-stream');
var sourcemaps = require('gulp-sourcemaps');
var uglify = require('gulp-uglify');
var util = require('gulp-util');
var watchify = require("watchify");

gulp.task('dev', function() {
    var b = getBrowserify();

    b = watchify(b);
    b.on("update", function() {
        bundle(b, {
            sourcemap : false,
            uglify : false
        });
    });
    b.on('log', util.log.bind(util));
    b.add("./src/main/es6/app.js");

    bundle(b, {
        sourcemap : false,
        uglify : false
    });
});

gulp.task('build', function() {
    var b = getBrowserify();
    bundle(b, {
        sourcemap : false,
        uglify : false
    });
});

gulp.task('clean', function () {
    del(['./src/main/webapp/public/dist/js']);
});

function getBrowserify() {
    return browserify('./src/main/es6/app.js', {
        debug: true
    })
    // .add(require.resolve('babel/polyfill'))
    .transform(babelify);
}

function bundle(b, options) {
    b = b.bundle()
        .on('error', util.log.bind(util, 'Browserify Error'))
        .pipe(source('app.js'))
        .pipe(buffer());

    if (options.sourcemap) {
        b = b.pipe(sourcemaps.init({
            loadMaps: true
        }));
    }

    if (options.uglify) {
        b = b.pipe(uglify({
            mangle: false
        }));
    }

    if (options.sourcemap) {
        b = b.pipe(sourcemaps.write('./'));
    }

    b.pipe(gulp.dest('./src/main/webapp/public/dist/js'));
}

gulp.task('default', ['dev']);
