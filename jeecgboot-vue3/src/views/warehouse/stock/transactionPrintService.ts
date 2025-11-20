import jsPDF from 'jspdf';
import 'jspdf-autotable';
import { stockTransactionApi } from './stockTransaction.api';

// Recordable type definition
type Recordable<T = any> = Record<string, T>;

interface TransactionPrintData {
  transaction: Recordable;
  items: Recordable[];
  companyInfo: Recordable;
}

/**
 * Print stock transaction to PDF
 * @param transactionId Transaction ID
 * @param printOptions Print options
 */
export async function printTransaction(transactionId: string, printOptions: any = {}) {
  try {
    // Get transaction details
    const result = await stockTransactionApi.queryById(transactionId);
    const transactionData: TransactionPrintData = {
      transaction: result.transaction,
      items: result.items || [],
      companyInfo: {
        name: 'Warehouse Management System',
        address: '123 Warehouse Street, City, Country',
        phone: '+1234567890',
        email: 'info@warehouse.com',
      },
    };

    // Generate PDF based on transaction type
    const doc = new jsPDF();
    
    // Add company header
    addCompanyHeader(doc, transactionData.companyInfo);
    
    // Add transaction title based on type
    const transactionType = transactionData.transaction.transactionType;
    let title = 'Transaction';
    if (transactionType === 'IN') {
      title = 'Stock In Transaction';
    } else if (transactionType === 'OUT') {
      title = 'Stock Out Transaction';
    } else if (transactionType === 'TRANSFER') {
      title = 'Stock Transfer Transaction';
    }
    
    doc.setFontSize(18);
    doc.text(title, 105, 70, { align: 'center' });
    
    // Add transaction details
    addTransactionDetails(doc, transactionData);
    
    // Add transaction items table
    addTransactionItemsTable(doc, transactionData.items);
    
    // Add footer
    addFooter(doc);
    
    // Save or print the PDF
    if (printOptions.save) {
      doc.save(`${transactionData.transaction.transactionCode}.pdf`);
    } else {
      doc.autoPrint();
    }
    
    return true;
  } catch (error) {
    console.error('Error printing transaction:', error);
    return false;
  }
}

/**
 * Add company header to PDF
 */
function addCompanyHeader(doc: any, companyInfo: Recordable) {
  doc.setFontSize(12);
  doc.text(companyInfo.name, 105, 20, { align: 'center' });
  doc.setFontSize(10);
  doc.text(companyInfo.address, 105, 30, { align: 'center' });
  doc.text(`Phone: ${companyInfo.phone}`, 105, 40, { align: 'center' });
  doc.text(`Email: ${companyInfo.email}`, 105, 50, { align: 'center' });
}

/**
 * Add transaction details to PDF
 */
function addTransactionDetails(doc: any, transactionData: TransactionPrintData) {
  const transaction = transactionData.transaction;
  
  doc.setFontSize(12);
  doc.text('Transaction Details:', 20, 90);
  
  doc.setFontSize(10);
  let yPosition = 100;
  
  doc.text(`Transaction Code: ${transaction.transactionCode || ''}`, 20, yPosition);
  yPosition += 10;
  
  doc.text(`Transaction Type: ${getTransactionTypeName(transaction.transactionType)}`, 20, yPosition);
  yPosition += 10;
  
  doc.text(`Status: ${getTransactionStatusName(transaction.status)}`, 20, yPosition);
  yPosition += 10;
  
  doc.text(`Transaction Date: ${transaction.transactionDate || ''}`, 20, yPosition);
  yPosition += 10;
  
  if (transaction.supplierName) {
    doc.text(`Supplier: ${transaction.supplierName}`, 20, yPosition);
    yPosition += 10;
  }
  
  if (transaction.createdBy) {
    doc.text(`Created By: ${transaction.createdBy}`, 20, yPosition);
    yPosition += 10;
  }
  
  if (transaction.approvedBy) {
    doc.text(`Approved By: ${transaction.approvedBy}`, 20, yPosition);
    yPosition += 10;
  }
  
  if (transaction.notes) {
    doc.text(`Notes: ${transaction.notes}`, 20, yPosition);
    yPosition += 10;
  }
  
  return yPosition;
}

/**
 * Add transaction items table to PDF
 */
function addTransactionItemsTable(doc: any, items: Recordable[]) {
  if (items.length === 0) return;
  
  // Prepare table data
  const tableData = items.map(item => [
    item.productCode || '',
    item.productName || '',
    item.quantity || 0,
    item.unitPrice || 0,
    item.totalPrice || 0,
    item.batchNumber || '',
    item.expiryDate || '',
  ]);
  
  // Add table headers
  const headers = [
    'Product Code',
    'Product Name',
    'Quantity',
    'Unit Price',
    'Total Price',
    'Batch Number',
    'Expiry Date',
  ];
  
  // Add table to PDF
  doc.autoTable({
    head: [headers],
    body: tableData,
    startY: 160,
    theme: 'grid',
    styles: {
      fontSize: 9,
      cellPadding: 3,
    },
    headStyles: {
      fillColor: [66, 66, 66],
      textColor: 255,
    },
    alternateRowStyles: {
      fillColor: [245, 245, 245],
    },
  });
  
  // Calculate and add total
  const totalAmount = items.reduce((sum, item) => sum + (item.totalPrice || 0), 0);
  const finalY = (doc as any).lastAutoTable.finalY || 160;
  
  doc.setFontSize(12);
  doc.text(`Total Amount: ${totalAmount.toFixed(2)}`, 150, finalY + 10);
}

/**
 * Add footer to PDF
 */
function addFooter(doc: any) {
  const pageCount = doc.getNumberOfPages();
  doc.setFontSize(10);
  doc.text(`Page ${pageCount}`, 105, 280, { align: 'center' });
  doc.text('This is a computer-generated document and does not require a signature.', 105, 290, { align: 'center' });
}

/**
 * Get transaction type name
 */
function getTransactionTypeName(type: string): string {
  switch (type) {
    case 'IN':
      return 'Stock In';
    case 'OUT':
      return 'Stock Out';
    case 'TRANSFER':
      return 'Stock Transfer';
    default:
      return type || '';
  }
}

/**
 * Get transaction status name
 */
function getTransactionStatusName(status: string): string {
  switch (status) {
    case 'PENDING':
      return 'Pending';
    case 'APPROVED':
      return 'Approved';
    case 'CANCELLED':
      return 'Cancelled';
    default:
      return status || '';
  }
}