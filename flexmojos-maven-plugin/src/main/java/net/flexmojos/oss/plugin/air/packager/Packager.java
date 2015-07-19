package net.flexmojos.oss.plugin.air.packager;

import java.io.File;

/**
 * Created by christoferdutz on 16.07.15.
 */
public interface Packager {

    void setRequest(PackagingRequest request);

    /**
     * Method that allows implmenting platform dependant validation of the
     * configuration.
     */
    void validateConfiguration() throws PackagingException;

    /**
     * Method to setup the work-directory as the adt packaging requires
     * a directory structure similar to the AIR sdk we only want to copy
     * and unpack stuff if it's not already there.
     * @return true if the directory was created, false if it was already there.
     * @throws PackagingException something went wrong.
     */
    boolean prepare() throws PackagingException;

    /**
     * Actually perform the packaging.
     * @throws PackagingException something went wrong.
     */
    File execute() throws PackagingException;

}
