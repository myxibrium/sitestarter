var Framework = {};

Framework.templates = {};

Framework.csrfHeader = null;
Framework.csrfParam = null;
Framework.csrfToken = null;

Framework.isLoggedIn = false;
Framework.currentUser = null;

// Utils

Framework.postBinary = function(url, file, callback) {
    var reader = new FileReader();
    reader.readAsBinaryString(file);
    reader.onload = function() {
        var result = event.target.result;
        var fileName = file.name;
        var fileType = file.type;
        var options = {
            url: url,
            type: "POST",
            contentType: "application/octet-stream",
            data: result,
            beforeSend: function(jqXHR) {
                jqXHR.setRequestHeader("X-FILENAME", fileName);
                jqXHR.setRequestHeader("X-MIMETYPE", fileType);
            },
            success: function(res, status, xhr) {
                callback(xhr.getResponseHeader("X-ID"));
            }
        };
        Framework.patchAjaxOptions(options);
        $.ajax(options);
    };
};

Framework.delete = function(options) {
    options.type = "DELETE";
    Framework.patchAjaxOptions(options);
    return $.ajax(options);
};

Framework.postJson = function(options) {
    options.type = "POST";
    options.contentType = "application/json";
    options.data = JSON.stringify(options.data);
    Framework.patchAjaxOptions(options);
    return $.ajax(options);
};

Framework.putJson = function(options) {
    options.type = "PUT";
    options.contentType = "application/json";
    options.data = JSON.stringify(options.data);
    Framework.patchAjaxOptions(options);
    return $.ajax(options);
};

Framework.getJson = function(options) {
    Framework.patchAjaxOptions(options);
    return $.ajax(options);
};

Framework.getHtml = function(options) {
    options.dataType = "html";
    Framework.patchAjaxOptions(options);
    return $.ajax(options);
};

Framework.patchAjaxOptions = function(options) {
    var otherError = options.error;
    options.error = function(xhr) {
        if (xhr.status == 403) {
            location.reload();
        }
        if (otherError) {
            otherError(xhr);
        }
    };

    var otherBeforeSend = options.beforeSend;
    options.beforeSend = function(xhr, settings) {
        xhr.setRequestHeader(Framework.csrfHeader, Framework.csrfToken);
        if (otherBeforeSend) {
            otherBeforeSend(xhr, settings);
        }
    };
};

// Authentication

Framework.loadCSRF = function() {
    $.ajax({
        url: '/api/security/csrf?cachebuster='+Math.random(),
        async: false,
        success: function(data) {
            Framework.csrfHeader = data.headerName;
            Framework.csrfParam = data.parameterName;
            Framework.csrfToken = data.token;
        },
        error: function(data) {
            alert("This website is currently down for maintenance. Please try again later.");
        }
    });
};

Framework.login = function(username, password) {
    $("#login-alert").addClass("hidden");
    Framework.postJson({
        url: '/api/login',
        async: false,
        data: {"username": username, "password": password},
        success: function(data) {
            window.location.hash = "#!/profile";
        },
        error: function(data) {
            $("#login-alert").text("Invalid username or password.");
            $("#login-alert").removeClass("hidden");
        }
    });
};

Framework.register = function(username, password, email) {
    return Framework.postJson({
        url: '/api/user',
        data: {username: username, password: password, email: email}
    });
};

Framework.getCurrentUser = function() {
    return Framework.getJson({
        url: '/api/user',
        success: function(data) {
            Framework.isLoggedIn = true;
            Framework.currentUser = data;
        },
        error: function(data) {
            Framework.isLoggedIn = false;
            Framework.currentUser = null;
        }
    });
};

Framework.logout = function() {
    return Framework.getJson({
        url: '/api/user/logout',
        success: function(data) {
            Framework.isLoggedIn = false;
            Framework.currentUser = null;
            location.hash = "#!/login";
        },
        error: function (data) {
            location.hash = "#!/login";
        }
    });
};

// Client-Side Templating

Framework.loadTemplate = function(name, callback) {
    if (Framework.templates[name]) {
        callback(Framework.templates[name]);
    } else {
        $.ajax({
            url: '/app/templates/' + name + '.html',
            success: function (data) {
                Framework.templates[name] = Handlebars.compile(data);
                callback(Framework.templates[name]);
            },
            error: function() {
                callback();
            }
        });
    }
};

Framework.Page = function(root, containerIds) {

    var internal = {};
    internal.containerIds = containerIds;

    internal.router = new Navigo(root, true, '#!');

    this.navigate = function(path) {
        if (location.hash === "#!"+path) {
            location.reload();
        } else {
            location.hash = "#!"+path;
        }
    };

    this.addRoute = function(path, templateMap, initFunc) {
        internal.router.on(path, function(params) {
            var callbackFunction = function (data, loginRequired) {
                var parentDef = $.Deferred();
                var chain = parentDef;
                if (!data) {
                    chain.resolve();
                } else if (loginRequired && !Framework.isLoggedIn) {
                    location.hash = "!/login";
                    chain.resolve();
                } else {
                    data.dynamicValues = DynamicValues;
                    data.isLoggedIn = Framework.isLoggedIn;
                    data.currentUser = Framework.currentUser;
                    $.each(internal.containerIds, function (_, id) {
                        chain = chain.then(function () {
                            var def = $.Deferred();
                            if (templateMap[id].startsWith("/api")) {
                                var pathVarName = templateMap[id].match(/.*[{]([a-z]+)[}]/)[1];

                                Framework.getJson({
                                    url: templateMap[id].replace("{" + pathVarName + "}", params[pathVarName]),
                                    success: function (result) {
                                        if (result.title) {
                                            $("title").text(DynamicValues.siteName + " - " + result.title);
                                        }
                                        $("#" + id).html("<div class='container'><h1>"+result.title+"</h1>\n"+result.mainContent+"</div>");
                                        def.resolve();
                                    },
                                    error: function() {
                                        def.resolve();
                                    }
                                })
                            } else {
                                Framework.loadTemplate(templateMap[id], function (template) {
                                    if (template) {
                                        $("#" + id).html(template(data));
                                    }
                                    def.resolve();
                                });
                            }
                            return def.promise();
                        });
                    });
                    parentDef.resolve();
                    if (data.title) {
                        $("title").text(DynamicValues.siteName + " - " + data.title);
                    }
                }

                var promise = chain.promise();

                return promise;
            };

            Framework.getCurrentUser().done(function() {
                initFunc(params, callbackFunction);
            }).fail(function() {
                initFunc(params, callbackFunction);
            });
        });
        return this;
    };

    this.start = function() {
        internal.router.resolve();
    };
};

// CMS Features

Framework.prepareVideos = function($container) {
    $container.fitVids();
    $container.find("iframe").attr("allowfullscreen", true);
};

Framework.prepareHtmlEditor = function($summernote) {
    $summernote.summernote({
        height: 500,
        width: "100%",
        callbacks: {
            onImageUpload: function (files) {
                var data = new FormData();
                jQuery.each(files, function (i, file) {
                    data.append('files', file);
                });
                $.ajax({
                    url: 'api/media',
                    data: data,
                    cache: false,
                    contentType: false,
                    processData: false,
                    method: 'POST',
                    beforeSend: function (jqXHR) {
                        jqXHR.setRequestHeader(Framework.csrfHeader, Framework.csrfToken);
                    },
                    success: function (res, status, xhr) {
                        $.each(res, function (i, pair) {
                            $imgNode = $("<a href='/api/media/"+pair.id+"'><img src='/api/media/" + pair.thumbnailId + "' /></a>");
                            $summernote.summernote('insertNode', $imgNode[0]);
                        });
                    }
                });
            }
        }
    });
};

// Initialize

Framework.loadCSRF();