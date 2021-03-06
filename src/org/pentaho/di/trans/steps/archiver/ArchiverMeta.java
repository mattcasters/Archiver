/**
 * 
 */
package org.pentaho.di.trans.steps.archiver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.ui.trans.steps.archiver.ArchiverDialog;
import org.w3c.dom.Node;

@Step(
		id="Archiver",
		name="Archiver.Step.Name",
		description="Archiver.Step.Description",
		categoryDescription="Archiver.Step.Category",
		image="org/pentaho/di/trans/steps/archiver/Archiver.png",
		i18nPackageName="org.pentaho.di.trans.steps.archiver",
		casesUrl="https://github.com/knowbi/Archiver/issues",
		documentationUrl="https://github.com/knowbi/Archiver/wiki/Documentation",
		forumUrl="https://github.com/knowbi/Archiver"
	)
public class ArchiverMeta extends BaseStepMeta implements StepMetaInterface {
  
  private static Class<?> PKG = ArchiverMeta.class;
  
  private List<ArchiverGeneration> generations;

  public ArchiverMeta() {
    super();
    clear();
  }
  
  public void clear() {
    generations = new ArrayList<ArchiverGeneration>();
  }

  

  public void setDefault() {
    clear();
  }

  public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
    clear();
    Node generationsNode = XMLHandler.getSubNode(stepnode, "generations");
    List<Node> generationNodes = XMLHandler.getNodes(generationsNode, ArchiverGeneration.XML_TAG);
    for (Node generationNode : generationNodes) {
      generations.add(new ArchiverGeneration(generationNode));
    }
  }
  
  @Override
  public String getXML() throws KettleException {
    StringBuilder xml = new StringBuilder();
    xml.append(XMLHandler.openTag("generations"));
    for (ArchiverGeneration generation : generations) {
      xml.append(generation.getXML());
    }
    xml.append(XMLHandler.closeTag("generations"));
    return xml.toString();
  }
  
  @Override
  public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException {
    ValueMetaInterface filename = new ValueMetaString("filename");
    inputRowMeta.addValueMeta(filename);
    
    ValueMetaInterface operation = new ValueMetaString("operation");
    inputRowMeta.addValueMeta(operation);
  }
  
  public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException {
    for (int i=0;i<generations.size();i++) {
      ArchiverGeneration generation = generations.get(i);
      generation.saveRep(rep, id_transformation, id_step, i);
    }
  }

  public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
    clear();
    int nrGenerations = rep.countNrStepAttributes(id_step, "source_folder");
    for (int i=0;i<nrGenerations;i++) {
      generations.add(new ArchiverGeneration(rep, id_step, i));
    }    
  }

  public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info) {
    for (ArchiverGeneration generation : generations) {
      if (Const.isEmpty(generation.getSourceFolder())) {
        remarks.add(new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "ArchiverMeta.CheckResult.EmptySourceFolder.ErrorMessage"), stepMeta));
      }
      if (Const.isEmpty(generation.getTargetFolder())) {
        remarks.add(new CheckResult(CheckResult.TYPE_RESULT_WARNING, BaseMessages.getString(PKG, "ArchiverMeta.CheckResult.EmptyTargetFolder.WarningMessage"), stepMeta));
      }
    }
  }

  public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
    return new Archiver(stepMeta, stepDataInterface, copyNr, transMeta, trans);
  }

  public StepDataInterface getStepData() {
    return new ArchiverData();
  }
  
  @Override
  public String getDialogClassName() {
  	return ArchiverDialog.class.getName();
  }

  public List<ArchiverGeneration> getGenerations() {
    return generations;
  }
  
  @Override
  public boolean supportsErrorHandling() {
    return true;
  }
}
