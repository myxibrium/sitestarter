
Site Starter
============

This is an experimental project. It aims to be a minimalistic Spring Boot site starter based on the following principals:

* Plain old SQL, using raw JDBC and Flyway for migrations.

* No server-side templating. The entire view is HTML/Javascript/Jquery with Navigo and Handlebars. (In retrospect, it would have been better to use Vue.js)

* Plain REST services on the server.

Using the app
-------------
1. Run the app with the local profile.
2. Go to the url: localhost:8080/#!/login
3. Put in username "admin" password "admin"
4. Add a new page.
5. Use the WYSIWYG editor to add content, attach images, and insert embedded videos.
6. Publish the page
7. Go back to the home page and see the content listed there.



Development
-----------

All templates start with a root "Page" which has all the necessary javascript imports and container objects which are filled by templates.

To register a page:

    var page = new Framework.Page(null, ['main-content']);

The above code does the following:

1. Creates a new page with auto-detection on root URL (null)

2. Defines all containers which may take a template (['main-content'])

To add a route:

    page.addRoute("/",
        {
            'main-content': 'test'
        },
        function(params, callback) {
            callback({'message': 'Hello, World!'});
        }
    );

The above code does the following:

1. Creates a new route on the root path ("/")

2. Defines which templates should be used in each container. In this example, the template "templates/test.html" will be used in the main-content container.

3. Defines an init function that defines the complete data model of the page. In this example, a single message field is added to the scope.

After routes have been added, ensure the page starts running:

    page.start();

The above code does the following:

1. Starts the Navigo routing engine.

