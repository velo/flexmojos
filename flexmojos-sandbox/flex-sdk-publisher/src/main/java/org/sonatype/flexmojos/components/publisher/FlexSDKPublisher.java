/**
 * Copyright 2008 Marvin Herman Froeder
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonatype.flexmojos.components.publisher;

import java.io.File;

public interface FlexSDKPublisher
{

    String ADOBE_GROUP_ID = "com.adobe.flex";

    String COMPILER_GROUP_ID = ADOBE_GROUP_ID + ".compiler";

    String FRAMEWORK_GROUP_ID = ADOBE_GROUP_ID + ".framework";

    String ROLE = FlexSDKPublisher.class.getName();

    void publish( File sdkFolder, String version, int defaultPlayer, File overwriteLibFolder )
        throws PublishingException;

}
