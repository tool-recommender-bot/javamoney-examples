// CSVImporter

package org.javamoney.examples.ez.money.importexport;

import static org.javamoney.examples.ez.money.importexport.CSVConstants.FILE_DESCRIPTION;
import static org.javamoney.examples.ez.money.importexport.CSVConstants.FILE_EXTENSION;
import static org.javamoney.examples.ez.money.importexport.DataMerger.mergeWithCollections;
import static org.javamoney.examples.ez.money.importexport.ImportExportFormatKeys.CSV;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collection;
import java.util.LinkedList;

import org.javamoney.examples.ez.money.exception.DialogCanceledException;
import org.javamoney.examples.ez.money.exception.NoAccountForImportException;
import org.javamoney.examples.ez.money.gui.dialog.ImportTransactionDialog;
import org.javamoney.examples.ez.money.model.dynamic.transaction.ImportTransaction;
import org.javamoney.examples.ez.money.model.persisted.account.Account;
import org.javamoney.examples.ez.money.model.persisted.transaction.Transaction;

/**
 * This class facilitates importing transactions from a CSV file that the user
 * selects from a dialog.
 */
public
final
class
CSVImporter
extends Importer
{
  /**
   * This method presents the user with a file dialog for choosing files to
   * import, and then imports the transactions from the selected files.
   */
  public
  void
  doImport()
  {
    doImport(CSV, FILE_EXTENSION, FILE_DESCRIPTION);
  }

  //////////////////////////////////////////////////////////////////////////////
  // Start of protected methods.
  //////////////////////////////////////////////////////////////////////////////

  /**
   * This method imports the transactions from the specified file and adds them
   * to the account specified in the file. If an account is not specified in the
   * file, a dialog will appear to allow the user to select one from a list of
   * available accounts.
   *
   * @param file The file to import transactions from.
   */
  @Override
  protected
  void
  importTransactions(File file)
  throws Exception
  {
    BufferedReader stream = new BufferedReader(new FileReader(file));
    Collection<ImportTransaction> list = new LinkedList<ImportTransaction>();
    CSVTransactionExtracter extracter = new CSVTransactionExtracter();
    Account account = chooseAccount();
    Transaction trans = null;

    // There must be an account to import transactions into.
    if(account == null)
    {
      throw new NoAccountForImportException();
    }

    // Get the transactions.
    try
    {
      list.add(new ImportTransaction(extracter.next(stream), extracter.getType()));
    }
    catch(Exception exception)
    {
      // Odds are there are column headers in the file, so ignore them.
    }

    while((trans = extracter.next(stream)) != null)
    {
      list.add(new ImportTransaction(trans, extracter.getType()));
    }

    // Close the stream.
    stream.close();

    // Allow the user to select which transactions to import.
    list = new ImportTransactionDialog(account, list).showDialog();

    // Either there is nothing to import or the dialog was canceled.
    if(list == null)
    {
      throw new DialogCanceledException();
    }

    // Add all the selected transactions into the account.
    for(ImportTransaction iTrans : list)
    {
      if(iTrans.isSelected() == true)
      {
        // Add the data in the transaction into the program's collections.
        mergeWithCollections(iTrans.getTransaction(), iTrans.isTransfer());

        addTransaction(account, iTrans);
      }
    }
  }
}
