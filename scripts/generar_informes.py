from pathlib import Path
import re, html, textwrap, posixpath
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Preformatted, PageBreak, KeepTogether, CondPageBreak, Image
from reportlab.lib import colors
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.enums import TA_LEFT
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.lib.pagesizes import A4

root=Path(__file__).resolve().parents[1]
for name, filename, fallback in [('Arial','arial.ttf','Helvetica'), ('ArialBold','arialbd.ttf','Helvetica-Bold'), ('Consolas','consola.ttf','Courier')]:
    font_path=Path('C:/Windows/Fonts')/filename
    if font_path.exists():
        pdfmetrics.registerFont(TTFont(name,str(font_path)))
    else:
        pdfmetrics.registerFont(pdfmetrics.Font(name,fallback,'WinAnsiEncoding'))
pdfmetrics.registerFontFamily('Arial',normal='Arial',bold='ArialBold',italic='Arial',boldItalic='ArialBold')
styles=getSampleStyleSheet()
styles.add(ParagraphStyle(name='BodyLab',fontName='Arial',fontSize=9.4,leading=13.4,spaceAfter=7,textColor=colors.HexColor('#243343'),splitLongWords=True))
styles.add(ParagraphStyle(name='TitleLab',fontName='ArialBold',fontSize=22,leading=27,spaceAfter=18,textColor=colors.HexColor('#133e59')))
styles.add(ParagraphStyle(name='H2Lab',fontName='ArialBold',fontSize=15,leading=19,spaceBefore=12,spaceAfter=11,textColor=colors.HexColor('#133e59'),keepWithNext=True))
styles.add(ParagraphStyle(name='H3Lab',fontName='ArialBold',fontSize=11,leading=15,spaceBefore=8,spaceAfter=6,textColor=colors.HexColor('#215b73'),keepWithNext=True))
styles.add(ParagraphStyle(name='CodeLab',fontName='Consolas',fontSize=7.3,leading=10,spaceBefore=5,spaceAfter=10,leftIndent=8,rightIndent=6,backColor=colors.HexColor('#f0f4f7'),borderPadding=7))
styles.add(ParagraphStyle(name='BulletLab',parent=styles['BodyLab'],leftIndent=10,firstLineIndent=-7))

def inline(text):
    s=html.escape(text)
    def link(m):
        label,url=m.group(1),html.unescape(m.group(2))
        if not url.startswith('http'):
            url='https://github.com/Konny-aguero/lab17/blob/main/'+posixpath.normpath('docs/'+url)
        return '<link href="'+html.escape(url,quote=True)+'" color="#17638a">'+label+'</link>'
    s=re.sub(r'\[([^\]]+)\]\(([^)]+)\)',link,s)
    s=re.sub(r'`([^`]+)`',r'<font name="Consolas" size="8.3">\1</font>',s)
    s=re.sub(r'\*\*([^*]+)\*\*',r'<b>\1</b>',s)
    return s

def footer(canvas,doc):
    width,height=A4
    canvas.saveState()
    canvas.setStrokeColor(colors.HexColor('#cbd7df'))
    canvas.line(42,height-35,width-42,height-35)
    canvas.setFont('ArialBold',8)
    canvas.setFillColor(colors.HexColor('#426072'))
    canvas.drawString(42,height-25,'LABORATORIO 17 / INSECUREPAY')
    canvas.setFont('Arial',8)
    canvas.drawString(42,25,'IF0009 - UCR | Pruebas: 14/11/2026 | Revision: 16/11/2026')
    canvas.drawRightString(width-42,25,str(doc.page))
    canvas.restoreState()

folders=['01_SQL_Injection','02_IDOR','03_Stored_XSS','04_BCrypt','05_Hardening']
report_path=root/'docs/BITACORA.md'
report=report_path.read_text(encoding='utf-8')
capture_index=iter(folders)
def capture_block(match):
    folder=next(capture_index)
    parts=['### Capturas manuales', '']
    for label, prefix in [('Antes','antes'),('Después','despues'),('Código','codigo_corregido')]:
        primary=root/'evidencias'/folder/(prefix+'.png')
        if primary.is_file():
            for photo in sorted(primary.parent.glob(prefix+'*.png')):
                parts.extend(['**'+label+' - captura real:**', '', '!['+photo.name+'](../evidencias/'+folder+'/'+photo.name+')', ''])
        else:
            parts.extend(['- '+label+' (`evidencias/'+folder+'/'+prefix+'.png`): PENDIENTE: INSERTAR CAPTURA REAL.', ''])
    parts.append('Las salidas reales ya incluidas cumplen la alternativa de evidencia textual descrita por el laboratorio. La guía explica cómo obtener las imágenes sin reproducir el estado vulnerable.')
    return '\n'.join(parts)+'\n'
updated=re.sub(r'### Capturas manuales\n.*?(?=\n## |\Z)',capture_block,report,flags=re.S)
if updated!=report:
    report_path.write_text(updated,encoding='utf-8')

for name in ['BITACORA','GUIA_CAPTURAS']:
    text=(root/'docs'/f'{name}.md').read_text(encoding='utf-8')
    story=[]
    paragraph=[]
    code=[]
    in_code=False
    def flush():
        if paragraph:
            story.append(Paragraph(inline(' '.join(paragraph)),styles['BodyLab']))
            paragraph.clear()
    for line in text.splitlines():
        if line == '<!-- SALTO DE PAGINA -->':
            flush()
            story.append(PageBreak())
            continue
        photo=re.fullmatch(r'!\[([^]]*)\]\(([^)]+)\)',line)
        if photo and not in_code:
            flush()
            photo_path=(root/'docs'/photo.group(2)).resolve()
            if not photo_path.is_relative_to(root/'evidencias'):
                raise ValueError('La captura debe estar dentro de evidencias: '+str(photo_path))
            if not photo_path.is_file():
                raise FileNotFoundError('Falta la captura: '+str(photo_path))
            figure=Image(str(photo_path))
            scale=min(505/figure.imageWidth,580/figure.imageHeight,1)
            figure.drawWidth=figure.imageWidth*scale
            figure.drawHeight=figure.imageHeight*scale
            story.append(KeepTogether([Paragraph(inline(photo.group(1)),styles['BodyLab']),figure,Spacer(1,12)]))
            continue
        if line.startswith('```'):
            flush()
            if in_code:
                wrapped=[]
                for code_line in code:
                    wrapped.extend(textwrap.wrap(code_line,width=112,replace_whitespace=False,drop_whitespace=False,subsequent_indent='    ') or [''])
                story.append(Preformatted('\n'.join(wrapped),styles['CodeLab']))
                code=[]
            in_code=not in_code
            continue
        if in_code:
            code.append(line)
            continue
        if not line.strip():
            flush()
        elif line.startswith('# '):
            flush()
            story.append(Paragraph(inline(line[2:]),styles['TitleLab']))
        elif line.startswith('## '):
            flush()
            heading=line[3:]
            if heading.startswith('HALLAZGO-') or (name=='GUIA_CAPTURAS' and re.match(r'[1-7]\. ',heading)):
                story.append(CondPageBreak(170))
            story.append(Paragraph(inline(heading),styles['H2Lab']))
        elif line.startswith('### '):
            flush()
            story.append(Paragraph(inline(line[4:]),styles['H3Lab']))
        elif line.startswith('- '):
            flush()
            story.append(Paragraph('• '+inline(line[2:]),styles['BulletLab']))
        elif re.match(r'\d+\. ',line):
            flush()
            story.append(Paragraph(inline(line),styles['BodyLab']))
        elif line.endswith('  '):
            flush()
            story.append(Paragraph(inline(line.strip()),styles['BodyLab']))
        else:
            paragraph.append(line)
    flush()
    document=SimpleDocTemplate(str(root/'docs'/f'{name}.pdf'),pagesize=A4,rightMargin=42,leftMargin=42,topMargin=49,bottomMargin=42,title='Laboratorio 17 - '+name.replace('_',' '),author='Konny Suseth Agüero Díaz',pageCompression=1)
    document.build(story,onFirstPage=footer,onLaterPages=footer)
    print(str(root/'docs'/f'{name}.pdf'))
