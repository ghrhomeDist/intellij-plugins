package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.lang.javascript.flex.IFlexSdkType;
import com.intellij.lang.javascript.flex.projectStructure.options.ModuleLibraryEntry;
import com.intellij.lang.javascript.flex.projectStructure.options.ProjectRootContainerModificator;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.projectRoots.SdkModificator;
import com.intellij.openapi.projectRoots.ex.ProjectRoot;
import com.intellij.openapi.projectRoots.impl.ProjectRootContainerImpl;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author ksafonov
 */
public class FlexSdk {

  public static final String SDK_ELEM = "sdk";
  private static final String HOME_ATTR = "home";

  @NotNull
  private final String myHomePath;

  @Nullable
  private String myFlexVersion;

  private final ProjectRootContainerImpl myRoots = new ProjectRootContainerImpl(true);

  public FlexSdk(@NotNull String homePath) {
    myHomePath = homePath;
  }

  public FlexSdk(Element element) throws InvalidDataException {
    String homePath = element.getAttributeValue(HOME_ATTR);
    if (StringUtil.isEmpty(homePath)) {
      throw new InvalidDataException("SDK home path is not defined");
    }
    myHomePath = homePath;
    myRoots.readExternal(element);
  }

  public Element getElement() throws WriteExternalException {
    Element element = new Element(SDK_ELEM);
    element.setAttribute("home", myHomePath);
    myRoots.writeExternal(element);
    return element;
  }

  public FlexSdk getCopy() {
    FlexSdk copy = new FlexSdk(myHomePath);
    ModuleLibraryEntry.copyContainer(myRoots, copy.myRoots);
    return copy;
  }

  @NotNull
  public String getHomePath() {
    return myHomePath;
  }

  public String[] getAllClassRoots() {
    Collection<String> urls = new ArrayList<String>();
    for (ProjectRoot root : myRoots.getRoots(OrderRootType.CLASSES)) {
      urls.addAll(Arrays.asList(root.getUrls()));
    }
    return ArrayUtil.toStringArray(urls);
  }

  @NotNull
  public String getFlexVersion() {
    if (myFlexVersion == null) {
      VirtualFile sdkRoot = LocalFileSystem.getInstance().findFileByPath(myHomePath);
      myFlexVersion = FlexSdkUtils.readFlexSdkVersion(sdkRoot);
    }
    return myFlexVersion;
  }

  public SdkModificator createRootsModificator() {
    return new ProjectRootContainerModificator(myHomePath, myRoots);
  }

  public boolean isValid() {
    VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(myHomePath);
    return vFile != null && FlexSdkUtils.isValidSdkRoot(FlexIdeUtils.getSdkType(), vFile);
  }
}
