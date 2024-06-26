package org.nmslite.apiserver;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.nmslite.Bootstrap;
import org.nmslite.db.ConfigDB;
import org.nmslite.utils.Constants;
import org.nmslite.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.nmslite.utils.RequestType.CREDENTIAL;

public class Credential {

    private static final Logger logger = LoggerFactory.getLogger(Credential.class);

    private final Router router;

    public Credential()
    {
        this.router = Router.router(Bootstrap.getVertx());
    }

    public  void init(Router router)
    {

        router.route(Constants.CREDENTIAL_ROUTE).subRouter(this.router);

        this.router.route().handler(BodyHandler.create());

        this.router.get(Constants.ROUTE_PATH).handler(this::getCredentials);

        this.router.post(Constants.ROUTE_PATH).handler(this::add);

    }

    private void getCredentials(RoutingContext context)
    {
        try
        {
            context.response().setStatusCode(Constants.OK);

            var response = ConfigDB.read(CREDENTIAL);

            response.put(Constants.ERROR_CODE, Constants.SUCCESS_CODE);

            response.put(Constants.STATUS, Constants.SUCCESS);

            context.json(response);
        }
        catch (Exception exception)
        {
            logger.error("Exception occurred while retrieving credentials", exception);

            var response = Utils.errorHandler(exception.toString(),Constants.BAD_REQUEST,exception.getMessage());

            context.response().setStatusCode(Constants.BAD_REQUEST);

            context.json(response);
        }
    }

    private void add(RoutingContext context)
    {
        var data = context.body().asJsonObject();
        try
        {
            if (!data.isEmpty())
            {

                if (data.containsKey(Constants.USERNAME) && data.containsKey(Constants.PASSWORD) && data.containsKey(Constants.NAME))
                {

                    if (!data.getString(Constants.USERNAME).isEmpty() && !data.getString(Constants.PASSWORD).isEmpty() && !data.getString(Constants.NAME).isEmpty())
                    {

                        var response = ConfigDB.create(CREDENTIAL, data);

                        if (response.containsKey(Constants.STATUS))
                        {
                            response = Utils.errorHandler("Credential Profile Not Created",Constants.BAD_REQUEST,"Credential with Name " + data.getString(Constants.NAME) + " already exists");

                            context.response().setStatusCode(Constants.BAD_REQUEST);
                        }
                        else
                        {
                            response.put(Constants.ERROR_CODE, Constants.SUCCESS_CODE);

                            response.put(Constants.STATUS, Constants.SUCCESS);

                            context.response().setStatusCode(Constants.OK);
                        }

                        context.json(response);

                    }
                    else
                    {

                        logger.error("Credentials are Invalid !!");

                        var response = new JsonObject();

                        response = Utils.errorHandler("Empty Fields",Constants.BAD_REQUEST,"Fields Can't Be Empty");

                        context.response().setStatusCode(Constants.BAD_REQUEST);

                        context.json(response);

                    }
                }
                else
                {
                    logger.error("Credentials are Missing in the Request !!");

                    var response = Utils.errorHandler("No Credentials Provided",Constants.BAD_REQUEST, "Provide Username and Password");

                    context.response().setStatusCode(Constants.BAD_REQUEST);

                    context.json(response);
                }
            }
            else
            {
                var response = Utils.errorHandler("Invalid JSON Format",Constants.BAD_REQUEST, "Provide Valid JSON Format");

                context.response().setStatusCode(Constants.BAD_REQUEST);

                context.json(response);

            }
        }
        catch (Exception exception)
        {
            logger.error("Error creating credential profile :", exception);

            var response = Utils.errorHandler(exception.toString(),Constants.BAD_REQUEST,exception.getMessage());

            context.response().setStatusCode(Constants.BAD_REQUEST);

            context.json(response);
        }
    }

}
