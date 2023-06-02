terraform {
  backend "s3" {
    bucket = "halo-tf-state"
    key = "global/web-app/terraform.tfstate"
    region = "us-east-1"
    dynamodb_table = "terraform_state_locking"
    encrypt = true
  }
}
provider "aws" {
  region = "us-east-1"
  access_key = "AKIA22QCIJF5SDECITOR"
  secret_key = "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"
}

data "aws_vpc" "default_vpc" {
    default = true
}

module "instance_security" {
  source = "./Instance_security"
}

resource "aws_instance" "Jenkins_server" {
  ami           = var.ami_id[1]
  instance_type = "t2.micro"
  key_name = "Spring"
  security_groups = [aws_security_group.instances_security.name]
  user_data       = "${file("user-datad.sh")}"
  tags = {
    name = "Jenkins_master_server"
  }
}





