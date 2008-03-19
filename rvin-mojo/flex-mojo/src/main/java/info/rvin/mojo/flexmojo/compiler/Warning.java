package info.rvin.mojo.flexmojo.compiler;

public class Warning {
    /**
     * Array.toString() format has changed.
     */
    private boolean arrayTostringChanges = false;

    /**
     * Assignment within conditional.
     */
    private boolean assignmentWithinConditional = true;

    /**
     * Possibly invalid Array cast operation.
     */
    private boolean badArrayCast = true;

    /**
     * Non Boolean value used where a Boolean value was expected.
     */
    private boolean badBooleanAssignment = true;

    /**
     * Invalid Date cast operation.
     */
    private boolean badDateCast = true;

    /**
     * Unknown method.
     */
    private boolean badEs3TypeMethod = true;

    /**
     * Unknown property.
     */
    private boolean badEs3TypeProp = true;

    /**
     * Illogical comparison with NaN. Any comparison operation involving NaN
     * will evaluate to false because NaN != NaN.
     */
    private boolean badNanComparison = true;

    /**
     * Impossible assignment to null.
     */
    private boolean badNullAssignment = true;

    /**
     * Illogical comparison with null.
     */
    private boolean badNullComparison = true;

    /**
     * Illogical comparison with undefined. Only untyped variables (or variables
     * of type *) can be undefined.
     */
    private boolean badUndefinedComparison = true;

    /**
     * Turn on reporting of data binding warnings. For example: Warning: Data
     * binding will not be able to detectAssignments to "foo".
     */
    private boolean binding = true;

    /**
     * Boolean() with no arguments returns false in ActionScript 3.0. Boolean()
     * returned undefined in ActionScript 2.0.
     */
    private boolean booleanConstructorWithNoArgs = false;

    /**
     * resolve is no longer supported.
     */
    private boolean changesInResolve = false;

    /**
     * Class is sealed. It cannot have members added to it dynamically.
     */
    private boolean classIsSealed = true;

    /**
     * Constant not initialized.
     */
    private boolean constNotInitialized = true;

    /**
     * Function used in new expression returns a value. Result will be what the
     * function returns, rather than a new instance of that function.
     */
    private boolean constructorReturnsValue = false;

    /**
     * EventHandler was not added as a listener.
     */
    private boolean deprecatedEventHandlerError = false;

    /**
     * Unsupported ActionScript 2.0 function.
     */
    private boolean deprecatedFunctionError = true;

    /**
     * Unsupported ActionScript 2.0 property.
     */
    private boolean deprecatedPropertyError = true;

    /**
     * More than one argument by the same name.
     */
    private boolean duplicateArgumentNames = true;

    /**
     * Duplicate variable definition
     */
    private boolean duplicateVariableDef = true;

    /**
     * ActionScript 3.0 iterates over an object's properties within a "for x in
     * target" statement in random order.
     */
    private boolean forVarInChanges = false;

    /**
     * Importing a package by the same name as the current class will hide that
     * class identifier in this scope.
     */
    private boolean importHidesClass = true;

    /**
     * Use of the instanceof operator.
     */
    private boolean instanceOfChanges = true;

    /**
     * Internal error in compiler.
     */
    private boolean internalError = true;

    /**
     * level is no longer supported. For more information, see the flash.display
     * package.
     */
    private boolean levelNotSupported = true;

    /**
     * Missing namespace declaration (e.g. variable is not defined to be public,
     * private, etc.).
     */
    private boolean missingNamespaceDecl = true;

    /**
     * Negative value will become a large positive value when assigned to a uint
     * data type.
     */
    private boolean negativeUintLiteral = true;

    /**
     * Missing constructor.
     */
    private boolean noConstructor = false;

    /**
     * The super() statement was not called within the constructor.
     */
    private boolean noExplicitSuperCallInConstructor = false;

    /**
     * Missing type declaration.
     */
    private boolean noTypeDecl = true;

    /**
     * In ActionScript 3.0, white space is ignored and '' returns 0. Number()
     * returns NaN in ActionScript 2.0 when the parameter is '' or contains
     * white space.
     */
    private boolean numberFromStringChanges = false;

    /**
     * Change in scoping for the this keyword. Class methods extracted from an
     * instance of a class will always resolve this back to that instance. In
     * ActionScript 2.0 this is looked up dynamically based on where the method
     * is invoked from.
     */
    private boolean scopingChangeInThis = false;

    /**
     * Inefficient use of += on a TextField.
     */
    private boolean slowTextFieldAddition = true;

    /**
     * Possible missing parentheses.
     */
    private boolean unlikelyFunctionValue = true;

    /**
     * toggle whether warnings generated from unused type selectors are
     * displayed
     */
    private boolean unusedTypeSelector = true;

    /**
     * Possible usage of the ActionScript 2.0 XML class.
     */
    private boolean xmlClassHasChanged = false;

    /**
     * Toggles whether the use of deprecated APIs generates a warning. This is
     * equivalent to using the <code>compiler.show-deprecation-warnings</code>
     * option of the mxmlc or compc compilers.
     */
    private boolean deprecation = true;

    /**
     * Toggles whether warnings are displayed when an embedded font name shadows
     * a device font name. This is equivalent to using the
     * <code>compiler.show-shadowed-device-font-warnings</code> option of the
     * mxmlc or compc compilers.
     */
    private boolean shadowedDeviceFont = true;

    public boolean getArrayTostringChanges() {
	return arrayTostringChanges;
    }

    public boolean getAssignmentWithinConditional() {
	return assignmentWithinConditional;
    }

    public boolean getBadArrayCast() {
	return badArrayCast;
    }

    public boolean getBadBooleanAssignment() {
	return badBooleanAssignment;
    }

    public boolean getBadDateCast() {
	return badDateCast;
    }

    public boolean getBadEs3TypeMethod() {
	return badEs3TypeMethod;
    }

    public boolean getBadEs3TypeProp() {
	return badEs3TypeProp;
    }

    public boolean getBadNanComparison() {
	return badNanComparison;
    }

    public boolean getBadNullAssignment() {
	return badNullAssignment;
    }

    public boolean getBadNullComparison() {
	return badNullComparison;
    }

    public boolean getBadUndefinedComparison() {
	return badUndefinedComparison;
    }

    public boolean getBinding() {
	return binding;
    }

    public boolean getBooleanConstructorWithNoArgs() {
	return booleanConstructorWithNoArgs;
    }

    public boolean getChangesInResolve() {
	return changesInResolve;
    }

    public boolean getClassIsSealed() {
	return classIsSealed;
    }

    public boolean getConstNotInitialized() {
	return constNotInitialized;
    }

    public boolean getConstructorReturnsValue() {
	return constructorReturnsValue;
    }

    public boolean getDeprecatedEventHandlerError() {
	return deprecatedEventHandlerError;
    }

    public boolean getDeprecatedFunctionError() {
	return deprecatedFunctionError;
    }

    public boolean getDeprecatedPropertyError() {
	return deprecatedPropertyError;
    }

    public boolean getDuplicateArgumentNames() {
	return duplicateArgumentNames;
    }

    public boolean getDuplicateVariableDef() {
	return duplicateVariableDef;
    }

    public boolean getForVarInChanges() {
	return forVarInChanges;
    }

    public boolean getImportHidesClass() {
	return importHidesClass;
    }

    public boolean getInstanceOfChanges() {
	return instanceOfChanges;
    }

    public boolean getInternalError() {
	return internalError;
    }

    public boolean getLevelNotSupported() {
	return levelNotSupported;
    }

    public boolean getMissingNamespaceDecl() {
	return missingNamespaceDecl;
    }

    public boolean getNegativeUintLiteral() {
	return negativeUintLiteral;
    }

    public boolean getNoConstructor() {
	return noConstructor;
    }

    public boolean getNoExplicitSuperCallInConstructor() {
	return noExplicitSuperCallInConstructor;
    }

    public boolean getNoTypeDecl() {
	return noTypeDecl;
    }

    public boolean getNumberFromStringChanges() {
	return numberFromStringChanges;
    }

    public boolean getScopingChangeInThis() {
	return scopingChangeInThis;
    }

    public boolean getSlowTextFieldAddition() {
	return slowTextFieldAddition;
    }

    public boolean getUnlikelyFunctionValue() {
	return unlikelyFunctionValue;
    }

    public boolean getUnusedTypeSelector() {
	return unusedTypeSelector;
    }

    public boolean getXmlClassHasChanged() {
	return xmlClassHasChanged;
    }

    public void setArrayTostringChanges(boolean arrayTostringChanges) {
	this.arrayTostringChanges = arrayTostringChanges;
    }

    public void setAssignmentWithinConditional(
	    boolean assignmentWithinConditional) {
	this.assignmentWithinConditional = assignmentWithinConditional;
    }

    public void setBadArrayCast(boolean badArrayCast) {
	this.badArrayCast = badArrayCast;
    }

    public void setBadBooleanAssignment(boolean badBoolAssignment) {
	this.badBooleanAssignment = badBoolAssignment;
    }

    public void setBadDateCast(boolean badDateCast) {
	this.badDateCast = badDateCast;
    }

    public void setBadEs3TypeMethod(boolean badEs3TypeMethod) {
	this.badEs3TypeMethod = badEs3TypeMethod;
    }

    public void setBadEs3TypeProp(boolean badEs3TypeProp) {
	this.badEs3TypeProp = badEs3TypeProp;
    }

    public void setBadNanComparison(boolean badNanComparison) {
	this.badNanComparison = badNanComparison;
    }

    public void setBadNullAssignment(boolean badNullAssignment) {
	this.badNullAssignment = badNullAssignment;
    }

    public void setBadNullComparison(boolean badNullComparison) {
	this.badNullComparison = badNullComparison;
    }

    public void setBadUndefinedComparison(boolean badUndefinedComparison) {
	this.badUndefinedComparison = badUndefinedComparison;
    }

    public void setBinding(boolean binding) {
	this.binding = binding;
    }

    public void setBooleanConstructorWithNoArgs(
	    boolean booleanConstructorWithNoArgs) {
	this.booleanConstructorWithNoArgs = booleanConstructorWithNoArgs;
    }

    public void setChangesInResolve(boolean changesInResolve) {
	this.changesInResolve = changesInResolve;
    }

    public void setClassIsSealed(boolean classIsSealed) {
	this.classIsSealed = classIsSealed;
    }

    public void setConstNotInitialized(boolean constNotInitialized) {
	this.constNotInitialized = constNotInitialized;
    }

    public void setConstructorReturnsValue(boolean constructorReturnsValue) {
	this.constructorReturnsValue = constructorReturnsValue;
    }

    public void setDeprecatedEventHandlerError(
	    boolean deprecatedEventHandlerError) {
	this.deprecatedEventHandlerError = deprecatedEventHandlerError;
    }

    public void setDeprecatedFunctionError(boolean deprecatedFunctionError) {
	this.deprecatedFunctionError = deprecatedFunctionError;
    }

    public void setDeprecatedPropertyError(boolean deprecatedPropertyError) {
	this.deprecatedPropertyError = deprecatedPropertyError;
    }

    public void setDuplicateArgumentNames(boolean duplicateArgumentNames) {
	this.duplicateArgumentNames = duplicateArgumentNames;
    }

    public void setDuplicateVariableDef(boolean duplicateVariableDef) {
	this.duplicateVariableDef = duplicateVariableDef;
    }

    public void setForVarInChanges(boolean forVarInChanges) {
	this.forVarInChanges = forVarInChanges;
    }

    public void setImportHidesClass(boolean importHidesClass) {
	this.importHidesClass = importHidesClass;
    }

    public void setInstanceOfChanges(boolean instanceOfChanges) {
	this.instanceOfChanges = instanceOfChanges;
    }

    public void setInternalError(boolean internalError) {
	this.internalError = internalError;
    }

    public void setLevelNotSupported(boolean levelNotSupported) {
	this.levelNotSupported = levelNotSupported;
    }

    public void setMissingNamespaceDecl(boolean missingNamespaceDecl) {
	this.missingNamespaceDecl = missingNamespaceDecl;
    }

    public void setNegativeUintLiteral(boolean negativeUintLiteral) {
	this.negativeUintLiteral = negativeUintLiteral;
    }

    public void setNoConstructor(boolean noConstructor) {
	this.noConstructor = noConstructor;
    }

    public void setNoExplicitSuperCallInConstructor(
	    boolean noExplicitSuperCallInConstructor) {
	this.noExplicitSuperCallInConstructor = noExplicitSuperCallInConstructor;
    }

    public void setNoTypeDecl(boolean noTypeDecl) {
	this.noTypeDecl = noTypeDecl;
    }

    public void setNumberFromStringChanges(boolean numberFromStringChanges) {
	this.numberFromStringChanges = numberFromStringChanges;
    }

    public void setScopingChangeInThis(boolean scopingChangeInThis) {
	this.scopingChangeInThis = scopingChangeInThis;
    }

    public void setSlowTextFieldAddition(boolean slowTextFieldAddition) {
	this.slowTextFieldAddition = slowTextFieldAddition;
    }

    public void setUnlikelyFunctionValue(boolean unlikelyFunctionValue) {
	this.unlikelyFunctionValue = unlikelyFunctionValue;
    }

    public void setUnusedTypeSelector(boolean unusedTypeSelector) {
	this.unusedTypeSelector = unusedTypeSelector;
    }

    public void setXmlClassHasChanged(boolean xmlClassHasChanged) {
	this.xmlClassHasChanged = xmlClassHasChanged;
    }

    public boolean getDeprecation() {
	return this.deprecation;
    }

    public boolean getShadowedDeviceFont() {
	return this.shadowedDeviceFont;
    }

}
