Site = {};

Site.mainPage = new Framework.Page(null, ['navbar', 'main-content']);

Site.mainPage.addRoute("/",
    {
        'navbar': 'core/navbar',
        'main-content': 'core/home'
    },
    function(params, callback) {
        Framework.getJson({
            url: "/api/page/home",
            success: function(data) {
                callback({
                    'title': "Home",
                    'message': 'This is the home page.',
                    'pages': data
                }).done(function() {
                    Framework.prepareVideos($("#main-content"));
                });
            },
            error: callback
        });
    });

Site.mainPage.addRoute("/login",
    {
        'navbar': 'core/navbar',
        'main-content': 'core/login'
    },
    function(params, callback) {
        callback({
            'title': "Login"
        }).done().then(function() {
            $("#form-login").submit(function() {
                var username = $(this).find(".input-username").val();
                var password = $(this).find(".input-password").val();
                Framework.login(username, password);
                return false;
            });
        });
    });

Site.mainPage.addRoute("/register",
    {
        'navbar': 'core/navbar',
        'main-content': 'core/register'
    },
    function(params, callback) {
        callback({
            'title': "Register"
        }, true).done().then(function () {
            $("#user-registration").submit(function() {
                var $register = $("#register-alert");
                $register.addClass("hidden");
                var username = $("#register-username").val();
                var email = $("#register-email").val();
                var password = $("#register-password").val();
                var passwordConfirm = $("#register-password-confirm").val();
                if (password != passwordConfirm) {
                    $register.removeClass("hidden");
                    $register.text("Passwords do not match.");
                } else {
                    Framework.register(username, password, email).done().then(function() {
                        window.location.hash = "!/verify";
                    });
                }
                return false;
            });
        });
    });

var submitEmailVerificationCode = function (code) {
    Framework.getJson({
        url: "/api/user/email/verify/" + code,
        success: function(data) {
            if (data.status === "SUCCESS") {
                $("#email-verification-success").removeClass("hidden");
            } else {
                $("#email-verification-error").removeClass("hidden");
            }
        }
    });
}

var initEmailVerificationPage = function() {
    $("#email-verification").submit(function() {
        $("#email-verification-error").addClass("hidden");
        $("#email-verification-success").addClass("hidden");
        submitEmailVerificationCode($("#email-verif-code").val());
        return false;
    });
};

Site.mainPage.addRoute("/verify",
    {
        'navbar': 'core/navbar',
        'main-content': 'core/verify'
    },
    function(params, callback) {
        callback({
            'title': "Verify Email"
        }).done(initEmailVerificationPage);
    });

Site.mainPage.addRoute("/verify/:code",
    {
        'navbar': 'core/navbar',
        'main-content': 'core/verify'
    },
    function(params, callback) {
        callback({
            'title': "Verify Email"
        }).done().then(function() {
            initEmailVerificationPage();
            $("#email-verif-code").val(params.code);
            submitEmailVerificationCode(params.code);
        });
    });

Site.mainPage.addRoute("/profile",
    {
        'navbar': 'core/navbar',
        'main-content': 'core/profile'
    },
    function(params, callback) {
        var pages = null;
        var media = null;
        var deferred = $.Deferred();

        var tryResolve = function() {
            if (pages != null && media != null) {
                deferred.resolve();
            }
        };

        Framework.getJson({
            url:"/api/user/pages",
            success: function(data) {
                pages = data;
                tryResolve();
            },
            error: function () {
                pages = [];
                tryResolve();
            }
        });

        Framework.getJson({
            url: "/api/user/media",
            success: function(data) {
                media = data;
                tryResolve();
            },
            error: function () {
                media = [];
                tryResolve();
            }
        });

        deferred.done(function() {
            callback({
                'title': "Profile",
                'hasPages': pages.length > 0,
                'pages': pages,
                'hasMedia': media.length > 0,
                'media': media
            }, true).then(function() {
                $(".delete-media").click(function () {
                    var mediaId = $(this).attr("data-id");
                    Framework.delete({
                        url: "/api/media/"+mediaId,
                        success: function() {location.reload()},
                        error: function() {location.reload()}
                    });
                });
            });
        });

    });

Site.mainPage.addRoute("/logout",
    {
        'navbar': 'core/navbar',
        'main-content': 'core/profile'
    },
    function(params, callback) {
        Framework.logout();
        callback();
    });

var initializePageForm = function() {
    Framework.prepareHtmlEditor($('#page-content'));

    var titleToName = function () {
        $("#page-name").val($("#page-title").val().replace(/[\s\-]+/g, "-").replace(/[^A-Za-z0-9\-]/g, "").toLowerCase());
    };

    var pageNameWasChangedManually = false;

    $("#page-title").bind("input", function () {
        if (!pageNameWasChangedManually) {
            titleToName();
        }
    });
    $("#page-name").bind("input", function (e) {
        if (e.originalEvent) {
            pageNameWasChangedManually = true;
        }
        $("#page-name").val($("#page-name").val().replace(/[\s\-]+/g, "-").replace(/[^A-Za-z0-9$\-_.+*'(),]/g, "").toLowerCase());
    });
    $("#page-name").blur(function () {
        if ($("#page-name").val() == "") {
            pageNameWasChangedManually = false;
            titleToName();
        }
    });
};

Site.mainPage.addRoute("/page/edit/:id",
    {
        'navbar': 'core/navbar',
        'main-content': 'core/content/page-form'
    },
    function(params, callback) {
        Framework.getJson({
            url: "/api/page/id_"+params.id,
            success: function(existing) {
                callback({
                    'title': "Edit Page",
                    'existing': existing
                }, true).done().then(function () {
                    $(function () {
                        initializePageForm();
                        $("#page-form").submit(function() {
                            $("#page-save-alert").addClass("hidden");
                            Framework.putJson({
                                url: "/api/page",
                                data: {
                                    id: $("#page-id").val(),
                                    title: $("#page-title").val(),
                                    name: $("#page-name").val(),
                                    mainContent: $('#page-content').summernote('code')
                                },
                                success: function() {
                                    location.hash = "!/page/view/"+$("#page-name").val();
                                },
                                error: function() {
                                    $("#page-save-alert").removeClass("hidden");
                                }
                            });
                        });
                    });
                });
            },
            error: callback
        });
    });

Site.mainPage.addRoute("/page/create",
    {
        'navbar': 'core/navbar',
        'main-content': 'core/content/page-form'
    },
    function(params, callback) {
        callback({
            'title': "Create Page"
        }, true).done().then(function () {
            $(function () {
                initializePageForm();
                $("#page-form").submit(function() {
                    $("#page-save-alert").addClass("hidden");
                    Framework.postJson({
                        url: "/api/page",
                        data: {
                            title: $("#page-title").val(),
                            name: $("#page-name").val(),
                            mainContent: $('#page-content').summernote('code')
                        },
                        success: function(data) {
                            location.hash = "!/page/edit/"+data;
                        },
                        error: function() {
                            $("#page-save-alert").removeClass("hidden");
                        }
                    });
                });
            });
        });
    });

Site.mainPage.addRoute("/page/view/:name",
    {
        'navbar': 'core/navbar',
        'main-content': '/api/page/name_{name}'
    },
    function(params, callback) {
        callback({});
    });

$(function() {
    Site.mainPage.start();
});