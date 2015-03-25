package com.proftaak.pts4.core.restlet;

import com.avaje.ebean.Ebean;
import com.proftaak.pts4.core.restlet.annotations.CRUDController;
import com.proftaak.pts4.core.restlet.annotations.PreRequest;
import com.proftaak.pts4.core.restlet.annotations.ProcessScopeObject;
import com.proftaak.pts4.database.tables.Token;
import com.proftaak.pts4.database.tables.User;
import flexjson.JSONSerializer;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.restlet.data.Status;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Michon
 */
public class RequestData {
    /**
     * The controller to which this object belongs.
     */
    private BaseController controller;

    /**
     * The parameter that have been passed in the URL
     */
    protected Map<String, Object> urlParams;

    /**
     * The payload that has been sent from the client
     */
    protected Map<String, Object> payload;

    /**
     * The currently logged in user, if any
     */
    protected User user;

    /**
     * The currently used token, if any
     */
    protected Token token;

    /**
     * The roles the current user has within the current scope.
     */
    private Collection<ScopeRole> roles = new TreeSet<>();

    /**
     * The JSONSerializer that will be used to serialize the returned object
     */
    private JSONSerializer jsonSerializer;

    protected RequestData(BaseController controller) {
        this.controller = controller;
        this.jsonSerializer = new JSONSerializer();
        this.jsonSerializer.exclude("class");
    }

    public Map<String, Object> getUrlParams() {
        return urlParams;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public User getUser() {
        return user;
    }

    public Token getToken() {
        return this.token;
    }

    public void addScopeRole(ScopeRole role) {
        this.roles.add(role);
    }

    public boolean hasScopeRole(ScopeRole role) {
        return this.roles.contains(role);
    }

    public Collection<ScopeRole> getScopeRoles() {
        return Collections.unmodifiableCollection(this.roles);
    }

    public JSONSerializer getSerializer() {
        return jsonSerializer;
    }

    /**
     * Get the object of the given type from the current request
     *
     * This object has to be available in the URL, and the type must be either the type of this controller, or of one of the parent controllers
     *
     * @param cls The type of object to get
     * @return The object
     * @throws HTTPException If the object cannot be found. This probably means the id in the URL is invalid, so let this propagate
     */
    public <O> O getScopeObject(Class<O> cls) throws Exception {
        return this.getScopeObject(cls, true);
    }

    /**
     * Get the object of the given type from the current request
     *
     * This object has to be available in the URL, and the type must be either the type of this controller, or of one of the parent controllers
     *
     * @param cls The type of object to get
     * @param required Whether the object is required. If this is true, it's considered a fatal error is the object does not exist
     * @return The object, if it was found, or null
     * @throws HTTPException If the object cannot be found. This probably means the id in the URL is invalid, so let this propagate
     */
    public <O> O getScopeObject(Class<O> cls, boolean required) throws Exception {
        // Get the object
        O obj = this.getScopeObjectFromController(cls, this.controller.getClass());
        if (obj == null) {
            if (required) {
                throw HTTPException.ERROR_OBJECT_NOT_FOUND;
            } else {
                return null;
            }
        }

        // Process the object
        this.processScopeObjectForController(obj, this.controller.getClass());

        // Return the object.
        return obj;
    }

    private <O> O getScopeObjectFromController(Class<O> cls, Class<? extends BaseController> controllerCls) throws Exception {
        // Get the annotation
        CRUDController crudAnnotation = controllerCls.getAnnotation(CRUDController.class);
        if (crudAnnotation == null) {
            throw new Exception("The controller needs to be a CRUD controller in order for this function to work.");
        }

        // Check whether the controller can provide the requested object
        if (crudAnnotation.table() == cls) {
            // Get the object from the database
            String name = crudAnnotation.name().isEmpty() ? cls.getSimpleName().toLowerCase() : crudAnnotation.name();
            Object id = getUrlParams().get(name + "Id");
            if (id != null) {
                return Ebean.find(cls, id);
            } else {
                return null;
            }
        } else if (crudAnnotation.parent() != null) {
            // Recurse into the parent, if there is one
            return this.getScopeObjectFromController(cls, crudAnnotation.parent());
        } else {
            // Requested object class cannot be provided
            throw new Exception("Unable to get scope object of type " + cls.getSimpleName());
        }
    }

    private <O> void processScopeObjectForController(O obj, Class<? extends BaseController> controllerCls) throws Exception {
        // Let the controller process the object
        try {
            Reflections r = new Reflections(ClasspathHelper.forClass(controllerCls), new MethodAnnotationsScanner());
            for (Method processor : r.getMethodsAnnotatedWith(ProcessScopeObject.class)) {
                if (processor.getDeclaringClass() == controllerCls && processor.getAnnotation(ProcessScopeObject.class).value() == obj.getClass()) {
                    processor.invoke(null, this, obj);
                }
            }
        } catch (InvocationTargetException e) {
            throw (Exception) e.getCause();
        }

        // If the controller has a parent, let that process the object too
        CRUDController crudAnnotation = controllerCls.getAnnotation(CRUDController.class);
        if (crudAnnotation != null && crudAnnotation.parent() != null) {
            this.processScopeObjectForController(obj, crudAnnotation.parent());
        }
    }
}
