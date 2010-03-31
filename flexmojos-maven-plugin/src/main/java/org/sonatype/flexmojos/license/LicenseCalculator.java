package org.sonatype.flexmojos.license;

import java.io.File;
import java.util.Map;

public interface LicenseCalculator
{
    Map<String, String> getInstalledLicenses();

    File getOSLicensePropertyFile();
}
