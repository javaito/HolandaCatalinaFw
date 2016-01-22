package org.hcjf.system;

import sun.reflect.Reflection;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 * This class loader verify all the classes loaded by the
 * system an instrument the different components.
 * @author javaito
 * @email javaito@gmail.com
 */
public class HCJFClassLoader extends ClassLoader {

    /**
     * Creates a new class loader using the specified parent class loader for
     * delegation.
     * <p>
     * <p> If there is a security manager, its {@link
     * SecurityManager#checkCreateClassLoader()
     * <tt>checkCreateClassLoader</tt>} method is invoked.  This may result in
     * a security exception.  </p>
     *
     * @param parent The parent class loader
     * @throws SecurityException If a security manager exists and its
     *                           <tt>checkCreateClassLoader</tt> method doesn't allow creation
     *                           of a new class loader.
     * @since 1.2
     */
    public HCJFClassLoader(ClassLoader parent) {
        super(parent);
    }

    /**
     * Sets the default assertion status for this class loader to
     * <tt>false</tt> and discards any package defaults or class assertion
     * status settings associated with the class loader.  This method is
     * provided so that class loaders can be made to ignore any command line or
     * persistent assertion status settings and "start with a clean slate."
     *
     * @since 1.4
     */
    @Override
    public void clearAssertionStatus() {
        super.clearAssertionStatus();
    }

    /**
     * Sets the desired assertion status for the named top-level class in this
     * class loader and any nested classes contained therein.  This setting
     * takes precedence over the class loader's default assertion status, and
     * over any applicable per-package default.  This method has no effect if
     * the named class has already been initialized.  (Once a class is
     * initialized, its assertion status cannot change.)
     * <p>
     * <p> If the named class is not a top-level class, this invocation will
     * have no effect on the actual assertion status of any class. </p>
     *
     * @param className The fully qualified class name of the top-level class whose
     *                  assertion status is to be set.
     * @param enabled   <tt>true</tt> if the named class is to have assertions
     *                  enabled when (and if) it is initialized, <tt>false</tt> if the
     *                  class is to have assertions disabled.
     * @since 1.4
     */
    @Override
    public void setClassAssertionStatus(String className, boolean enabled) {
        super.setClassAssertionStatus(className, enabled);
    }

    /**
     * Sets the package default assertion status for the named package.  The
     * package default assertion status determines the assertion status for
     * classes initialized in the future that belong to the named package or
     * any of its "subpackages".
     * <p>
     * <p> A subpackage of a package named p is any package whose name begins
     * with "<tt>p.</tt>".  For example, <tt>javax.swing.text</tt> is a
     * subpackage of <tt>javax.swing</tt>, and both <tt>java.util</tt> and
     * <tt>java.lang.reflect</tt> are subpackages of <tt>java</tt>.
     * <p>
     * <p> In the event that multiple package defaults apply to a given class,
     * the package default pertaining to the most specific package takes
     * precedence over the others.  For example, if <tt>javax.lang</tt> and
     * <tt>javax.lang.reflect</tt> both have package defaults associated with
     * them, the latter package default applies to classes in
     * <tt>javax.lang.reflect</tt>.
     * <p>
     * <p> Package defaults take precedence over the class loader's default
     * assertion status, and may be overridden on a per-class basis by invoking
     * {@link #setClassAssertionStatus(String, boolean)}.  </p>
     *
     * @param packageName The name of the package whose package default assertion status
     *                    is to be set. A <tt>null</tt> value indicates the unnamed
     *                    package that is "current"
     *                    (see section 7.4.2 of
     *                    <cite>The Java&trade; Language Specification</cite>.)
     * @param enabled     <tt>true</tt> if classes loaded by this classloader and
     *                    belonging to the named package or any of its subpackages will
     *                    have assertions enabled by default, <tt>false</tt> if they will
     *                    have assertions disabled by default.
     * @since 1.4
     */
    @Override
    public void setPackageAssertionStatus(String packageName, boolean enabled) {
        super.setPackageAssertionStatus(packageName, enabled);
    }

    /**
     * Sets the default assertion status for this class loader.  This setting
     * determines whether classes loaded by this class loader and initialized
     * in the future will have assertions enabled or disabled by default.
     * This setting may be overridden on a per-package or per-class basis by
     * invoking {@link #setPackageAssertionStatus(String, boolean)} or {@link
     * #setClassAssertionStatus(String, boolean)}.
     *
     * @param enabled <tt>true</tt> if classes loaded by this class loader will
     *                henceforth have assertions enabled by default, <tt>false</tt>
     *                if they will have assertions disabled by default.
     * @since 1.4
     */
    @Override
    public void setDefaultAssertionStatus(boolean enabled) {
        super.setDefaultAssertionStatus(enabled);
    }

    /**
     * Returns the absolute path name of a native library.  The VM invokes this
     * method to locate the native libraries that belong to classes loaded with
     * this class loader. If this method returns <tt>null</tt>, the VM
     * searches the library along the path specified as the
     * "<tt>java.library.path</tt>" property.
     *
     * @param libname The library name
     * @return The absolute path of the native library
     * @see System#loadLibrary(String)
     * @see System#mapLibraryName(String)
     * @since 1.2
     */
    @Override
    protected String findLibrary(String libname) {
        return super.findLibrary(libname);
    }

    /**
     * Returns all of the <tt>Packages</tt> defined by this class loader and
     * its ancestors.
     *
     * @return The array of <tt>Package</tt> objects defined by this
     * <tt>ClassLoader</tt>
     * @since 1.2
     */
    @Override
    protected Package[] getPackages() {
        return super.getPackages();
    }

    /**
     * Returns a <tt>Package</tt> that has been defined by this class loader
     * or any of its ancestors.
     *
     * @param name The package name
     * @return The <tt>Package</tt> corresponding to the given name, or
     * <tt>null</tt> if not found
     * @since 1.2
     */
    @Override
    protected Package getPackage(String name) {
        return super.getPackage(name);
    }

    /**
     * Defines a package by name in this <tt>ClassLoader</tt>.  This allows
     * class loaders to define the packages for their classes. Packages must
     * be created before the class is defined, and package names must be
     * unique within a class loader and cannot be redefined or changed once
     * created.
     *
     * @param name        The package name
     * @param specTitle   The specification title
     * @param specVersion The specification version
     * @param specVendor  The specification vendor
     * @param implTitle   The implementation title
     * @param implVersion The implementation version
     * @param implVendor  The implementation vendor
     * @param sealBase    If not <tt>null</tt>, then this package is sealed with
     *                    respect to the given code source {@link URL
     *                    <tt>URL</tt>}  object.  Otherwise, the package is not sealed.
     * @return The newly defined <tt>Package</tt> object
     * @throws IllegalArgumentException If package name duplicates an existing package either in this
     *                                  class loader or one of its ancestors
     * @since 1.2
     */
    @Override
    protected Package definePackage(String name, String specTitle, String specVersion, String specVendor, String implTitle, String implVersion, String implVendor, URL sealBase) throws IllegalArgumentException {
        return super.definePackage(name, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor, sealBase);
    }

    /**
     * Returns an input stream for reading the specified resource.
     * <p>
     * <p> The search order is described in the documentation for {@link
     * #getResource(String)}.  </p>
     *
     * @param name The resource name
     * @return An input stream for reading the resource, or <tt>null</tt>
     * if the resource could not be found
     * @since 1.1
     */
    @Override
    public InputStream getResourceAsStream(String name) {
        return super.getResourceAsStream(name);
    }

    /**
     * Returns an enumeration of {@link URL <tt>URL</tt>} objects
     * representing all the resources with the given name. Class loader
     * implementations should override this method to specify where to load
     * resources from.
     *
     * @param name The resource name
     * @return An enumeration of {@link URL <tt>URL</tt>} objects for
     * the resources
     * @throws IOException If I/O errors occur
     * @since 1.2
     */
    @Override
    protected Enumeration<URL> findResources(String name) throws IOException {
        return super.findResources(name);
    }

    /**
     * Finds the resource with the given name. Class loader implementations
     * should override this method to specify where to find resources.
     *
     * @param name The resource name
     * @return A <tt>URL</tt> object for reading the resource, or
     * <tt>null</tt> if the resource could not be found
     * @since 1.2
     */
    @Override
    protected URL findResource(String name) {
        return super.findResource(name);
    }

    /**
     * Finds all the resources with the given name. A resource is some data
     * (images, audio, text, etc) that can be accessed by class code in a way
     * that is independent of the location of the code.
     * <p>
     * <p>The name of a resource is a <tt>/</tt>-separated path name that
     * identifies the resource.
     * <p>
     * <p> The search order is described in the documentation for {@link
     * #getResource(String)}.  </p>
     *
     * @param name The resource name
     * @return An enumeration of {@link URL <tt>URL</tt>} objects for
     * the resource.  If no resources could  be found, the enumeration
     * will be empty.  Resources that the class loader doesn't have
     * access to will not be in the enumeration.
     * @throws IOException If I/O errors occur
     * @apiNote When overriding this method it is recommended that an
     * implementation ensures that any delegation is consistent with the {@link
     * #getResource(String) getResource(String)} method. This should
     * ensure that the first element returned by the Enumeration's
     * {@code nextElement} method is the same resource that the
     * {@code getResource(String)} method would return.
     * @see #findResources(String)
     * @since 1.2
     */
    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return super.getResources(name);
    }

    /**
     * Finds the resource with the given name.  A resource is some data
     * (images, audio, text, etc) that can be accessed by class code in a way
     * that is independent of the location of the code.
     * <p>
     * <p> The name of a resource is a '<tt>/</tt>'-separated path name that
     * identifies the resource.
     * <p>
     * <p> This method will first search the parent class loader for the
     * resource; if the parent is <tt>null</tt> the path of the class loader
     * built-in to the virtual machine is searched.  That failing, this method
     * will invoke {@link #findResource(String)} to find the resource.  </p>
     *
     * @param name The resource name
     * @return A <tt>URL</tt> object for reading the resource, or
     * <tt>null</tt> if the resource could not be found or the invoker
     * doesn't have adequate  privileges to get the resource.
     * @apiNote When overriding this method it is recommended that an
     * implementation ensures that any delegation is consistent with the {@link
     * #getResources(String) getResources(String)} method.
     * @since 1.1
     */
    @Override
    public URL getResource(String name) {
        return super.getResource(name);
    }

    /**
     * Finds the class with the specified <a href="#name">binary name</a>.
     * This method should be overridden by class loader implementations that
     * follow the delegation model for loading classes, and will be invoked by
     * the {@link #loadClass <tt>loadClass</tt>} method after checking the
     * parent class loader for the requested class.  The default implementation
     * throws a <tt>ClassNotFoundException</tt>.
     *
     * @param name The <a href="#name">binary name</a> of the class
     * @return The resulting <tt>Class</tt> object
     * @throws ClassNotFoundException If the class could not be found
     * @since 1.2
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

    /**
     * Returns the lock object for class loading operations.
     * For backward compatibility, the default implementation of this method
     * behaves as follows. If this ClassLoader object is registered as
     * parallel capable, the method returns a dedicated object associated
     * with the specified class name. Otherwise, the method returns this
     * ClassLoader object.
     *
     * @param className The name of the to-be-loaded class
     * @return the lock for class loading operations
     * @throws NullPointerException If registered as parallel capable and <tt>className</tt> is null
     * @see #loadClass(String, boolean)
     * @since 1.7
     */
    @Override
    protected Object getClassLoadingLock(String className) {
        return super.getClassLoadingLock(className);
    }

    /**
     * Loads the class with the specified <a href="#name">binary name</a>.  The
     * default implementation of this method searches for classes in the
     * following order:
     * <p>
     * <ol>
     * <p>
     * <li><p> Invoke {@link #findLoadedClass(String)} to check if the class
     * has already been loaded.  </p></li>
     * <p>
     * <li><p> Invoke the {@link #loadClass(String) <tt>loadClass</tt>} method
     * on the parent class loader.  If the parent is <tt>null</tt> the class
     * loader built-in to the virtual machine is used, instead.  </p></li>
     * <p>
     * <li><p> Invoke the {@link #findClass(String)} method to find the
     * class.  </p></li>
     * <p>
     * </ol>
     * <p>
     * <p> If the class was found using the above steps, and the
     * <tt>resolve</tt> flag is true, this method will then invoke the {@link
     * #resolveClass(Class)} method on the resulting <tt>Class</tt> object.
     * <p>
     * <p> Subclasses of <tt>ClassLoader</tt> are encouraged to override {@link
     * #findClass(String)}, rather than this method.  </p>
     * <p>
     * <p> Unless overridden, this method synchronizes on the result of
     * {@link #getClassLoadingLock <tt>getClassLoadingLock</tt>} method
     * during the entire class loading process.
     *
     * @param name    The <a href="#name">binary name</a> of the class
     * @param resolve If <tt>true</tt> then resolve the class
     * @return The resulting <tt>Class</tt> object
     * @throws ClassNotFoundException If the class could not be found
     */
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return super.loadClass(name, resolve);
    }

    /**
     * Loads the class with the specified <a href="#name">binary name</a>.
     * This method searches for classes in the same manner as the {@link
     * #loadClass(String, boolean)} method.  It is invoked by the Java virtual
     * machine to resolve class references.  Invoking this method is equivalent
     * to invoking {@link #loadClass(String, boolean) <tt>loadClass(name,
     * false)</tt>}.
     *
     * @param name The <a href="#name">binary name</a> of the class
     * @return The resulting <tt>Class</tt> object
     * @throws ClassNotFoundException If the class was not found
     */
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {


            System.out.println(name);


        return super.loadClass(name);
    }

}
