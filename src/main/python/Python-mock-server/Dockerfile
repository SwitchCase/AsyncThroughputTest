# FROM python:3.7.9-alpine3.12
# FROM fnndsc/ubuntu-python3:latest
FROM python:3.10-slim

RUN mkdir /opt/app

COPY requirements.txt /opt/app
WORKDIR /opt/app

RUN pip install -r requirements.txt

COPY . /opt/app

EXPOSE 8080
CMD ["python", "src/mock-server-aa/server.py"]

# CMD ["/bin/sh"]