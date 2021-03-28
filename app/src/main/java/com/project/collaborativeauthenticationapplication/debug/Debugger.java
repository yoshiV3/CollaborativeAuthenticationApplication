package com.project.collaborativeauthenticationapplication.debug;

import com.project.collaborativeauthenticationapplication.logger.AndroidLogger;
import com.project.collaborativeauthenticationapplication.logger.Logger;
import com.project.collaborativeauthenticationapplication.service.crypto.BigNumber;
import com.project.collaborativeauthenticationapplication.service.crypto.CryptoKeyPartGenerator;
import com.project.collaborativeauthenticationapplication.service.crypto.CryptoKeyShareGenerator;
import com.project.collaborativeauthenticationapplication.service.crypto.CryptoProcessor;
import com.project.collaborativeauthenticationapplication.service.crypto.Point;

import java.util.ArrayList;
import java.util.LinkedList;

public class Debugger {


    private Logger logger = new AndroidLogger();
    private  static CryptoKeyPartGenerator  partGenerator  = new CryptoKeyPartGenerator();
    private  static CryptoKeyShareGenerator shareGenerator = new CryptoKeyShareGenerator();
    private static CryptoProcessor          processor      = new CryptoProcessor();

    private LinkedList<Test> tests = new LinkedList<>();

    private Debuggable child;
    public Debugger(Debuggable debugActivity) {
        child = debugActivity;
        buildTests();
    }


    public void runNextTest()
    {
        Test current    = tests.removeFirst();
        boolean success = current.run();
        String message;
        if (success)
        {
            message = "Successfully ran test: ";
        }
        else
        {
            message = "unsuccessfully ran test: ";
        }
        message += String.valueOf(current.getSequenceNumber());
        Signal result   =  new Signal(message, !success);

        boolean done = (tests.size() == 0);

        child.handleResult(result, done);
    }

    private void buildTests()
    {
        tests.add(new Test() {
            @Override
            public int getSequenceNumber() {
                return 1;
            }

            @Override
            public boolean run() {
                ArrayList<BigNumber> poly = new ArrayList<>();
                int degree = 4;

                byte[] one = new byte[32];
                one[0] = 1;
                for (int index = 1; index <32; index++)
                {
                    one[index] = 0;
                }
                BigNumber oneBnN = new BigNumber(one);

                for (int exp =0; exp <degree; exp++)
                {
                    poly.add(BigNumber.getZero());
                }

                poly.add(oneBnN);

                ArrayList<BigNumber> result = partGenerator.generate(poly, 1,1);
                logger.logEvent("DEBUGGER", "RESULT RECEIVED", "high", result.get(0).toString());
                return  result.get(0).getPart(0)[0] == 1 && result.get(0).getPart(0)[2] == 0;
            }
           }
        );


        tests.add(new Test() {
                      @Override
                      public int getSequenceNumber() {
                          return 2;
                      }

                      @Override
                      public boolean run() {
                          ArrayList<BigNumber> poly = new ArrayList<>();
                          int degree = 4;
                          byte[] one = new byte[32];
                          one[0] = 2;
                          for (int index = 1; index <32; index++)
                          {
                              one[index] = 0;
                          }
                          BigNumber oneBnN = new BigNumber(one);
                          for (int exp =0; exp <4; exp++)
                          {
                              poly.add(BigNumber.getZero());
                          }
                          poly.add(3,oneBnN);
                          ArrayList<BigNumber> result = partGenerator.generate(poly, 1,1);
                          logger.logEvent("DEBUGGER", "RESULT RECEIVED", "high", result.get(0).toString());
                          return  result.get(0).getPart(0)[0] == 2 && result.get(0).getPart(0)[1] == 0;
                      }
                  }
        );




        tests.add(new Test() {
                      @Override
                      public int getSequenceNumber() {
                          return 3;
                      }

                      @Override
                      public boolean run() {
                          ArrayList<BigNumber> poly = new ArrayList<>();
                          int degree = 4;
                          byte[] one = new byte[32];
                          for (int index = 0; index <32; index++)
                          {
                              one[index] = 0;
                          }
                          one[1] = 1;
                          BigNumber oneBnN = new BigNumber(one);
                          for (int exp =0; exp <4; exp++)
                          {
                              poly.add(BigNumber.getZero());
                          }
                          poly.add(oneBnN);
                          ArrayList<BigNumber> result = partGenerator.generate(poly, 1,1);
                          logger.logEvent("DEBUGGER", "RESULT RECEIVED", "high", result.get(0).toString());
                          return  result.get(0).getPart(0)[1] == 1 && result.get(0).getPart(0)[0] == 0;
                      }
                  }
        );


        tests.add(new Test() {
                      @Override
                      public int getSequenceNumber() {
                          return 4;
                      }

                      @Override
                      public boolean run() {
                          ArrayList<BigNumber> poly = new ArrayList<>();
                          int degree = 4;
                          byte[] one = new byte[32];
                          for (int index = 0; index <32; index++)
                          {
                              one[index] = 0;
                          }
                          one[5] = 1;
                          BigNumber oneBnN = new BigNumber(one);
                          for (int exp =0; exp <4; exp++)
                          {
                              poly.add(BigNumber.getZero());
                          }
                          poly.add(oneBnN);
                          ArrayList<BigNumber> result = partGenerator.generate(poly, 1,1);
                          logger.logEvent("DEBUGGER", "RESULT RECEIVED", "high", result.get(0).toString());
                          return  result.get(0).getPart(1)[1] == 1 && result.get(0).getPart(0)[0] == 0;
                      }
                  }
        );


        tests.add(new Test() {
                      @Override
                      public int getSequenceNumber() {
                          return 5;
                      }

                      @Override
                      public boolean run() {
                          ArrayList<BigNumber> poly = new ArrayList<>();
                          int degree = 4;
                          byte[] one = new byte[32];
                          for (int index = 0; index <32; index++)
                          {
                              one[index] = 1;
                          }
                          one[5] = 3;
                          BigNumber oneBnN = new BigNumber(one);
                          for (int exp =0; exp <4; exp++)
                          {
                              poly.add(BigNumber.getZero());
                          }
                          poly.add(oneBnN);
                          ArrayList<BigNumber> result = partGenerator.generate(poly, 1,1);
                          logger.logEvent("DEBUGGER", "RESULT RECEIVED", "high", result.get(0).toString());
                          return  result.get(0).getPart(1)[1] == 3 && result.get(0).getPart(1)[0] == 1;
                      }
                  }
        );

        tests.add(new Test() {
            @Override
            public int getSequenceNumber() {
                return 6;
            }

            @Override
            public boolean run() {
                ArrayList<ArrayList<BigNumber>> parts  = new ArrayList<>();
                ArrayList<BigNumber>            shares;

                byte[] one = new byte[32];
                for (int index = 0; index <32; index++)
                {
                    one[index] = 0;
                }
                one[0] = 1;
                BigNumber oneBnN = new BigNumber(one);

                ArrayList<BigNumber> first = new ArrayList<>();

                first.add(new BigNumber(one));

                int NB_PARTS = 20;
                for (int i = 0; i < NB_PARTS; i++ )
                {
                    first.add(BigNumber.getZero());
                }
                parts.add(first);
                shares = shareGenerator.generate(parts);
                logger.logEvent("DEBUGGER", "RESULT RECEIVED", "high", shares.get(0).toString());
                return shares.get(0).getPart(0)[0] == 1;
            }
        });

        tests.add(new Test() {
            @Override
            public int getSequenceNumber() {
                return 7;
            }

            @Override
            public boolean run() {
                ArrayList<ArrayList<BigNumber>> parts  = new ArrayList<>();
                ArrayList<BigNumber>            shares;

                byte[] one = new byte[32];
                for (int index = 0; index <32; index++)
                {
                    one[index] = 0;
                }
                one[6] = 1;
                BigNumber oneBnN = new BigNumber(one);

                ArrayList<BigNumber> first = new ArrayList<>();

                first.add(new BigNumber(one));

                int NB_PARTS = 20;
                for (int i = 0; i < NB_PARTS; i++ )
                {
                    first.add(BigNumber.getZero());
                }
                parts.add(first);
                shares = shareGenerator.generate(parts);
                logger.logEvent("DEBUGGER", "RESULT RECEIVED", "high", shares.get(0).toString());
                return shares.get(0).getPart(1)[2] == 1;
            }
        });


        tests.add(new Test() {
            @Override
            public int getSequenceNumber() {
                return 8;
            }

            @Override
            public boolean run() {
                ArrayList<ArrayList<BigNumber>> parts  = new ArrayList<>();
                ArrayList<BigNumber>            shares;

                byte[] one = new byte[32];
                for (int index = 0; index <32; index++)
                {
                    one[index] = 0;
                }
                one[6] = 1;
                BigNumber oneBnNOne = new BigNumber(one);
                one[0] = 1;
                BigNumber oneBnNTwo = new BigNumber(one);

                ArrayList<BigNumber> first = new ArrayList<>();
                ArrayList<BigNumber> second = new ArrayList<>();

                first.add(oneBnNOne);
                second.add(oneBnNTwo);


                int NB_PARTS = 20;
                for (int i = 0; i < NB_PARTS; i++ )
                {
                    first.add(BigNumber.getZero());
                    second.add(BigNumber.getZero());
                }
                parts.add(first);
                parts.add(second);
                shares = shareGenerator.generate(parts);
                logger.logEvent("DEBUGGER", "RESULT RECEIVED", "high", shares.get(0).toString());
                boolean result =  shares.get(0).getPart(1)[2] == 1;
                result         = result && (shares.get(1).getPart(0)[0] == 1) && shares.get(1).getPart(1)[2] == 1;
                return result;
            }
        });



        tests.add(new Test() {
                      @Override
                      public int getSequenceNumber() {
                          return 9;
                      }

                      @Override
                      public boolean run() {
                          ArrayList<ArrayList<BigNumber>> polynomials = new ArrayList<>();
                          ArrayList<BigNumber> poly = new ArrayList<>();
                          int degree = 4;

                          byte[] one = new byte[32];
                          one[0] = 1;
                          for (int index = 1; index <32; index++)
                          {
                              one[index] = 0;
                          }
                          BigNumber oneBnN = new BigNumber(one);

                          for (int exp =0; exp <degree; exp++)
                          {
                              poly.add(BigNumber.getZero());
                          }

                          poly.add(oneBnN);
                          polynomials.add(poly);

                          ArrayList<BigNumber> result_secrets = new ArrayList<>();
                          Point result_public = new Point(BigNumber.getZero(), BigNumber.getZero(), true);

                          processor.generateParts(6, polynomials, result_secrets, result_public);
                          logger.logEvent("DEBUGGER", "RESULT RECEIVED", "high", result_secrets.get(0).toString());
                          logger.logEvent("DEBUGGER", "RESULT RECEIVED", "high", result_public.getX().toString());
                          boolean correct = result_secrets.size() == 6;
                          correct = correct && result_secrets.get(0).getPart(0)[0] == 1;
                          correct = correct && result_secrets.get(0).getPart(1)[0] == 0;
                          correct = correct && result_public.getX().getPart(0)[0] == -104;
                          return correct;

                      }
                  }
        );




        tests.add(new Test() {
                      @Override
                      public int getSequenceNumber() {
                          return 10;
                      }

                      @Override
                      public boolean run() {
                          ArrayList<ArrayList<BigNumber>> polynomials = new ArrayList<>();
                          ArrayList<BigNumber> poly = new ArrayList<>();
                          int degree = 4;

                          byte[] one = new byte[32];
                          one[0] = 1;
                          for (int index = 1; index <32; index++)
                          {
                              one[index] = 0;
                          }
                          BigNumber oneBnN = new BigNumber(one);

                          for (int exp =0; exp <degree; exp++)
                          {
                              poly.add(BigNumber.getZero());
                          }

                          poly.add(oneBnN);
                          polynomials.add(poly);

                          poly = new ArrayList<>();

                          byte[] oneBis = new byte[32];
                          oneBis[0] = 1;
                          for (int index = 1; index <32; index++)
                          {
                              oneBis[index] = 0;
                          }
                          oneBnN = new BigNumber(oneBis);

                          for (int exp =0; exp <degree; exp++)
                          {
                              poly.add(BigNumber.getZero());
                          }

                          poly.add(oneBnN);
                          polynomials.add(poly);


                          ArrayList<BigNumber> result_secrets = new ArrayList<>();
                          Point result_public = new Point(BigNumber.getZero(), BigNumber.getZero(), true);

                          processor.generateParts(6, polynomials, result_secrets, result_public);
                          logger.logEvent("DEBUGGER", "RESULT RECEIVED", "high", result_secrets.get(0).toString());
                          logger.logEvent("DEBUGGER", "RESULT RECEIVED", "high", result_public.getX().toString());
                          boolean correct = result_secrets.size() == 6;
                          correct = correct && result_secrets.get(0).getPart(0)[0] == 2;
                          correct = correct && result_secrets.get(0).getPart(1)[0] == 0;
                          correct = correct && result_public.getX().getPart(0)[0] == -27;
                          return correct;

                      }
                  }
        );



    }

    private interface Test
    {
        int getSequenceNumber();
        boolean run();
    }
}
