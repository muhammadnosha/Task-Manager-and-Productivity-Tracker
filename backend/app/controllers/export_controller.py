import io
import csv
from fastapi.responses import StreamingResponse
from app.models import task as task_model

from reportlab.lib.pagesizes import letter
from reportlab.platypus import SimpleDocTemplate, Table, TableStyle
from reportlab.lib import colors

def generate_csv_of_tasks(user_id: int):
    tasks = task_model.get_tasks_by_user_id(user_id)
    
    output = io.StringIO()
    writer = csv.writer(output)
    
    writer.writerow(['ID', 'Title', 'Description', 'Priority', 'Deadline', 'Status', 'Time Spent (Seconds)'])
    
    for task in tasks:
        status = "Completed" if task.get('is_completed') else "Pending"
        writer.writerow([
            task.get('id'),
            task.get('title'),
            task.get('description', ''),
            task.get('priority', 'Medium'),
            task.get('deadline', 'N/A'),
            status,
            task.get('time_spent_seconds', 0)
        ])
    
    output.seek(0)
    
    response = StreamingResponse(iter([output.getvalue()]), media_type="text/csv")
    response.headers["Content-Disposition"] = "attachment; filename=tasks.csv"
    return response

def generate_pdf_of_tasks(user_id: int):
    tasks = task_model.get_tasks_by_user_id(user_id)
    
    buffer = io.BytesIO()
    doc = SimpleDocTemplate(buffer, pagesize=letter)
    
    data = [['ID', 'Title', 'Priority', 'Status', 'Time Spent']]
    for task in tasks:
        status = "Completed" if task.get('is_completed') else "Pending"
        time_spent_seconds = task.get('time_spent_seconds', 0)
        hours = time_spent_seconds // 3600
        minutes = (time_spent_seconds % 3600) // 60
        time_str = f"{hours}h {minutes}m"
        
        data.append([
            str(task.get('id')),
            task.get('title'),
            task.get('priority', 'Medium'),
            status,
            time_str
        ])
        
    table = Table(data)
    
    style = TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), colors.grey),
        ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
        ('ALIGN', (0, 0), (-1, -1), 'CENTER'),
        ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
        ('BOTTOMPADDING', (0, 0), (-1, 0), 12),
        ('BACKGROUND', (0, 1), (-1, -1), colors.beige),
        ('GRID', (0, 0), (-1, -1), 1, colors.black)
    ])
    table.setStyle(style)
    
    elements = [table]
    doc.build(elements)
    
    buffer.seek(0)
    
    response = StreamingResponse(buffer, media_type="application/pdf")
    response.headers["Content-Disposition"] = "attachment; filename=tasks.pdf"
    return response